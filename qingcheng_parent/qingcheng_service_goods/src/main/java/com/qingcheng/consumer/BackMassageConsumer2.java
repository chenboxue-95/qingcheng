//package com.qingcheng.consumer;
//
//import com.alibaba.dubbo.config.annotation.Reference;
//import com.alibaba.fastjson.JSON;
//import com.qingcheng.pojo.order.OrderItem;
//import com.qingcheng.service.goods.StockBackService;
//import com.qingcheng.service.pay.WeixinPayService;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageListener;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//
///**
// * 微信支付关闭订单
// */
//public class BackMassageConsumer2 implements MessageListener {
//
//    @Reference
//    private WeixinPayService weixinPayService;
//
//    @Override
//    public void onMessage(Message message) {
//        //1取出队列信息
//        String orderId =new String(message.getBody());
//        // 2调用查询订单状态方法
//        boolean flag = weixinPayService.findWxOrder(orderId);
//        if(flag){
//            // 3如果订单未支付 关闭订单
//            weixinPayService.closeWxOrder(orderId);
//        }else { // 已支付   补偿....
//
//        }
//
//
//    }
//}
