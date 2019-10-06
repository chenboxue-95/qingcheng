package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderItemService;
import com.qingcheng.service.order.PreferentialService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据登录人去redis查询对应购物车列表
     *
     * @param username 登录人
     * @return 购物车列表
     */
    @Override
    public List<Map<String, Object>> findCartList(String username) {
        System.out.println("从redis中提取购物车.");
        List<Map<String, Object>> cartList = (List<Map<String, Object>>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Reference
    private SkuService skuService;

    @Reference
    private CategoryService categoryService;

    /**
     * 添加商品到购物车
     *
     * @param username 用户名
     * @param skuId    商品skuId
     * @param num      商品数量
     */
    @Override
    public void addItem(String username, String skuId, Integer num) {
        //实现思路: 提取购物车,看其否有相同的商品,如果有则增加商品数量,如果没有则添加商品
        // 提取购物车
        List<Map<String, Object>> cartList = findCartList(username);
        boolean flag = false;
        for (Map map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");//每个订单
            if (orderItem.getSkuId().equals(skuId)) {
                //1 存在相同商品  添加数量
                if (orderItem.getNum() <= 0) { //如果商品数量小于该商品
                    cartList.remove(map); //移除商品
                    flag = true;
                    break;
                }
                int weight = orderItem.getWeight() / orderItem.getNum();//更新之前单品重量

                orderItem.setNum(orderItem.getNum() + num); //更新数量
                orderItem.setMoney(orderItem.getNum() * orderItem.getPrice()); //更新总价
                orderItem.setWeight(weight * orderItem.getNum()); //更新重量

                if (orderItem.getNum() <= 0) { //如果商品数量小于该商品
                    cartList.remove(map); //移除商品

                }
                flag = true;
                break;
            }

        }
        if (flag == false) {
            //2 不存在相同商品 新增商品
            // 2.2 根据skuId查询对应Sku
            Sku sku = skuService.findById(skuId);
            if (sku == null) {
                throw new RuntimeException("商品数量不存在!");
            }
            if (!"1".equals(sku.getStatus())) {
                throw new RuntimeException("商品状态不合法!");
            }
            if (num <= 0) {
                throw new RuntimeException("商品数量不合法!");
            }
            // 2.1 添加商品信息
            OrderItem orderItem = new OrderItem(); //每个订单信息
            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setNum(num);
            orderItem.setImage(sku.getImage());
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(orderItem.getPrice() * num);//单个商品总金额
            if (sku.getWeight() == null) {
                sku.setWeight(0);
            }
            orderItem.setWeight(sku.getWeight());

            // 2.3 设置商品分类
            orderItem.setCategoryId3(sku.getCategoryId()); //设置三级分类
            Category category3 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());
            if (category3 == null) {
                //缓存没有 去数据库查找并且放入缓存中
                category3 = categoryService.findById(sku.getCategoryId());//根据三级分类Id查询三级分类实体类
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(), category3);
            }
            orderItem.setCategoryId2(category3.getParentId()); //设置二级分类
            Category category2 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category3.getParentId());
            if (category2 == null) {
                //缓存没有 去数据库查找并且放入缓存中
                category2 = categoryService.findById(category3.getParentId());///获得二级分类实体类
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(), category2);
            }
            orderItem.setCategoryId1(category2.getParentId()); //设置一级分类
            System.out.println("订单为:" + orderItem.toString());


            Map map = new HashMap();//每个商品
            map.put("item", orderItem);
            map.put("checked", true);


            cartList.add(map);
        }
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);

    }

    /**
     * 更新选中状态
     *
     * @param username 用户名
     * @param skuId    商品skuId
     * @param checked  选中状态
     * @return
     */
    @Override
    public boolean updateChecked(String username, String skuId, boolean checked) {
        //提取购物车
        List<Map<String, Object>> cartList = findCartList(username);
        boolean isOk = false;
        for (Map map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");
            if (orderItem.getSkuId().equals(skuId)) {
                map.put("checked", checked);
                isOk = true; //执行成功
                break;

            }
            ;

        }
        //更新缓存
        if (isOk) {
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
        }
        return false;
    }

    @Autowired
    private PreferentialService preferentialService;

    /**
     * 删除选中购物车
     *
     * @param username 用户名
     */
    @Override
    public void deleteCheckedCart(String username) {
        //删除选中购物车,保存获取未选中的购物车
        List<Map<String, Object>> cartList = findCartList(username).stream().filter(cart -> (boolean) cart.get("checked") == false).collect(Collectors.toList());

        //加入缓存
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);

    }

    /**
     * 计算当前购物车的优惠金额
     * @param username 用户名
     * @return 优惠金额
     */
    @Override
    public int preferential(String username) {
        //获取选中购物车   List<OrderItem>  List<Map>
        List<OrderItem> orderItemList = findCartList(username).stream()
                .filter(cart -> (boolean) cart.get("checked") == true)  //获取选中
                .map(cart -> (OrderItem) cart.get("item"))  //获取订单
                .collect(Collectors.toList());//转为集合
        //分类 金额
        //1   20
        //2   52
        //  按分类聚合计算每个分类的金额
        Map<Integer, IntSummaryStatistics> cartMap = orderItemList.stream().
                collect(Collectors.groupingBy(OrderItem::getCategoryId3, Collectors.summarizingInt(OrderItem::getMoney)));
        int allMoney=0;  //累计优惠金额
        //循环结果 统计结果相加
        for (Integer categoryId : cartMap.keySet()) {
            int money = (int)cartMap.get(categoryId).getSum();
            int preMoney = preferentialService.findPreMoneyByCategoryId(categoryId, money); //获取优惠金额
            System.out.println("消费金额:"+money+"分类"+categoryId+"优惠金额"+preMoney);
            allMoney+=preMoney;

        }

        return allMoney;
    }

    /**
     *  获得最新购物车列表
     * @param username 用户名
     * @return 购物车列表
     */
    @Override
    public List<Map<String, Object>> findNewOrderItemList(String username) {
        //1 获得原来购物车
        List<Map<String, Object>> cartList = findCartList(username);
        //2 更新购物车
        for (Map map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");
            Sku sku = skuService.findById(orderItem.getSkuId());//获取数据库的商品
            orderItem.setPrice(sku.getPrice());  //更新价格
            orderItem.setMoney(orderItem.getPrice()*orderItem.getNum());  //更新金额

        }
        //3 保存购物车
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
        return cartList;
    }
}
