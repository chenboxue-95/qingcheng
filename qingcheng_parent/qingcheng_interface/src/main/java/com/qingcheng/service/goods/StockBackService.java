package com.qingcheng.service.goods;

import com.qingcheng.pojo.order.OrderItem;

import java.util.List;

public interface StockBackService {

    /**
     * 添加需要回滚订单日志
     * @param orderItems 需要回滚订单
     */
    public void addList(List<OrderItem> orderItems);

    /**
     * 回滚
     */
    public void back();

}
