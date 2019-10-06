package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderResults;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    /**
     * 保存订单
     * @param order 订单
     * @return 订单号 和金额
     */
    public Map<String,Object> add(Order order);


    public void update(Order order);


    public void delete(String id);

//  根据ID查询订单和订单详情
    public OrderResults findOrderResultById(String id);

//   根据订单ID数据查询订单集合
    public List<Order> findOrderListByIds(String consignStatus,String[] ids);

//    根据选择的订单集合  来进行发货处理  (记录日志)
    public void batchSend(List<Order> orders);

//  超时处理
    public void orderTimeOutLogic();

    /**
     * 修改订单流水号
     * @param orderId 订单号
     * @param transactionId 交易Id(流水线)
     */
    public void updatePayStatus(String orderId,String transactionId);

}
