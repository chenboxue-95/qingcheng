package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderConfigMapper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.util.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 返回全部记录
     *
     * @return
     */
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    @Autowired
    private CartService cartService;

    @Reference
    private SkuService skuService;


    /**
     * 新增
     *
     * @param order
     */
    public Map<String, Object> add(Order order) {
        //1.1 获取选中购物车
        List<Map<String, Object>> newOrderItemList = cartService.findNewOrderItemList(order.getUsername());
        //1.2 获取选中购物车
        List<OrderItem> orderItems = newOrderItemList.stream().filter(cart -> (boolean) cart.get("checked") == true)
                .map(cart -> (OrderItem) cart.get("item"))
                .collect(Collectors.toList());
        //2 构建库存
        if (!skuService.deductionStock(orderItems)) {
            throw new RuntimeException("库存不足!");
        }

        try {
            //3.1 保存订单主表
            order.setId(idWorker.nextId() + "");
            // 合计数计算
            IntStream numStream = orderItems.stream().mapToInt(OrderItem::getNum);
            IntStream moneyStream = orderItems.stream().mapToInt(OrderItem::getMoney);
            int totalSum = numStream.sum();//总数量
            int totalMoney = moneyStream.sum();//总金额
            int preMoney = cartService.preferential(order.getUsername());
            order.setTotalNum(totalSum); //总数量
            order.setTotalMoney(totalMoney);//总金额
            order.setPreMoney(preMoney);//优惠金额
            order.setPayMoney(totalMoney - preMoney);//支付金额=总金额-优惠金额
            order.setCreateTime(new Date());//创建时间
            order.setOrderStatus("0"); //订单状态: 待付款
            order.setPayStatus("0"); //支付状态:未支付
            order.setConsignStatus("0"); //发货状态 未发货
            orderMapper.insert(order);

            //3.2 将订单号发送给RabbitMQ
//            rabbitTemplate.convertAndSend("","queue.order",JSON.toJSONString(order.getId()));//将订单号发送给RabbitMQ 10有效



            double daZhe = (double) order.getPayMoney() / totalSum;
            // 4 保存订单明细表
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrderId(order.getId());//订单主表Id
                orderItem.setId(idWorker.nextId() + "");//订单明细表Id
                orderItem.setPayMoney((int) (orderItem.getMoney() * daZhe)); //支付金额
                orderItemMapper.insert(orderItem);// 添加详情
            }
        } catch (Exception e) {
            e.printStackTrace();
            rabbitTemplate.convertAndSend("", "queue.skuback", JSON.toJSONString(orderItems));
            throw new RuntimeException("订单生成失败");  //刨除异常 让其回滚
        }

        //5  清空购物车
        cartService.deleteCheckedCart(order.getUsername());

        //6 返回订单号和金额
        Map<String, Object> map = new HashMap<>();
        map.put("ordersn", order.getId());
        map.put("money", order.getPayMoney());

        return map;

    }

    /**
     * 修改
     *
     * @param order
     */
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据id查找订单和订单详情
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public OrderResults findOrderResultById(String id) {
//       查询订单
        Order order = orderMapper.selectByPrimaryKey(id);

//        查询对应订单详情
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId", id);
        List<OrderItem> orderItems = orderItemMapper.selectByExample(example);

//        封装实体类返回
        OrderResults orderResults = new OrderResults();
        orderResults.setOrder(order);
        orderResults.setOrderItemList(orderItems);

        return orderResults;
    }

    /**
     * //   根据订单ID数据查询订单集合
     *
     * @param consignStatus
     * @param ids
     * @return
     */
    @Override
    public List<Order> findOrderListByIds(String consignStatus, String[] ids) {
        //  设置条件
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("consignStatus", consignStatus);
        criteria.andIn("id", Arrays.asList(ids));
//        根据条件查询
        List<Order> orders = orderMapper.selectByExample(example);
        return orders;
    }


    /**
     * 根据订单集合  来进行发货处理
     *
     * @param orders
     */
    @Override
    public void batchSend(List<Order> orders) {
//        判断单号和物流公司是否为空
        for (Order order : orders) {
            if (order.getShippingCode() == null || order.getShippingName() == null) {
                throw new RuntimeException("请填写单号和物流公司");
            }
        }
//        循环更新订单
        for (Order order : orders) {
            order.setOrderStatus("2");//订单状态  已发货
            order.setConsignStatus("1");//发货状态  已发货
            order.setConsignTime(new Date());//发货时间
            orderMapper.updateByPrimaryKey(order);


//        记录日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOperater("zj");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(order.getId());
            orderLog.setOrderStatus(order.getOrderStatus());
            orderLog.setPayStatus("1");
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks("支付流水号" + order.getTransactionId());
            orderLogMapper.insert(orderLog);

        }


    }

    /**
     * 超时订单处理
     */
    @Override
    public void orderTimeOutLogic() {
//        订单超时未付款 自动关闭

        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey("1");//查找设置对象
        Integer orderTimeout = orderConfig.getOrderTimeout();//得到超时时间
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);//得到超时时间点

//        设置查询条件
        Example example = new Example(Example.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("createTime", localDateTime);//小于超时时间
        criteria.andEqualTo("orderStatus", "0");//未付款的
        criteria.andEqualTo("isDelete", "0");//未删除的
//      查询超时订单
        List<Order> orders = orderMapper.selectByExample(example);
        for (Order order : orders) {
//            记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOperater("system");//系统
            orderLog.setOperateTime(new Date());//当前日期
            orderLog.setOrderStatus("4");
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks("超时订单，系统自动关闭");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);
//            更改订单状态
            order.setOrderStatus("4");
            order.setCloseTime(new Date());//关闭日期
            orderMapper.updateByPrimaryKey(order);

        }


    }

    /**
     * 修改订单流水号
     *
     * @param orderId       订单号
     * @param transactionId 交易Id(流水线)
     */
    @Override
    public void updatePayStatus(String orderId, String transactionId) {
        // 1 查找符合条件的订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order != null && "0".equals(order.getPayStatus())) { //存在订单且支付状态为0 未支付
            //2 修改订单状态
            order.setPayStatus("1"); //支付状态
            order.setOrderStatus("1"); //订单状态
            order.setUpdateTime(new Date()); //更新时间
            order.setTransactionId(transactionId); //订单流水号
            orderMapper.updateByPrimaryKeySelective(order);  //根据原有字段更新

            //3 添加订单日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");//设置ID
            orderLog.setOperater("system"); //系统
            orderLog.setOperateTime(new Date()); //当前时间
            orderLog.setPayStatus("1"); //支付状态
            orderLog.setOrderStatus("1"); //订单状态
            orderLog.setRemarks("支付流水号: " + transactionId);//订单流水号
            orderLog.setOrderId(order.getId());//订单Id
            orderLogMapper.insert(orderLog);//添加日志

        }


    }

    /**
     * 构建查询条件
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andLike("id", "%" + searchMap.get("id") + "%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andLike("payType", "%" + searchMap.get("payType") + "%");
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andLike("sourceType", "%" + searchMap.get("sourceType") + "%");
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andLike("orderStatus", "%" + searchMap.get("orderStatus") + "%");
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andLike("payStatus", "%" + searchMap.get("payStatus") + "%");
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andLike("consignStatus", "%" + searchMap.get("consignStatus") + "%");
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andLike("isDelete", "%" + searchMap.get("isDelete") + "%");
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }
}


