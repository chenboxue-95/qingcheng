package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import com.qingcheng.pojo.goods.StockBack;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BackMassageConsumer implements MessageListener {
    @Autowired
    private StockBackService stockBackService;

    @Override
    public void onMessage(Message message) {
        try {
            //取出消息
            String jsonString = new String(message.getBody());
            List<OrderItem> orderItemList = JSON.parseArray(jsonString, OrderItem.class);
            //调用方法 写入回滚日志
            stockBackService.addList(orderItemList);
        } catch (Exception e) {
            e.printStackTrace();
            //记录日志..人工干预.....
        }


    }
}
