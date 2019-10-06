package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTask {

    @Reference
    private OrderService orderService;

    @Scheduled(cron = "0 0/2 * * * ?")
    public void orderTimeOutLogic(){
        System.out.println("11111");
        orderService.orderTimeOutLogic();

    }

}
