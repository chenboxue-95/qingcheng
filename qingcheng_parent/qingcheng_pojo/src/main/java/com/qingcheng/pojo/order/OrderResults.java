package com.qingcheng.pojo.order;

import java.io.Serializable;
import java.util.List;

/**
 * 订单列表和详情组合实体类
 */
public class OrderResults implements Serializable {
    private Order order;
    private List<OrderItem> orderItemList;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
