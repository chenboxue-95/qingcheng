package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.seckill.SeckillOrder;
import com.qingcheng.service.pay.WeixinPayService;
import com.qingcheng.service.seckill.SecKillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private SecKillOrderService secKillOrderService;

    @Reference
    private WeixinPayService weixinPayService;

    /**
     * 生成二维码
     *
     * @return
     */
    @GetMapping("/createNative")
    public Map createNative() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        SeckillOrder seckillOrder = secKillOrderService.findById(username);

        if (seckillOrder != null) {
            //校验该订单是否时当前用户的订单
            if (username.equals(seckillOrder)) {
                int money = (int) ((seckillOrder.getMoney().doubleValue()) * 100);


                return weixinPayService.createNative(seckillOrder.getId().toString(), money,
                        "http://chenboxuw.cross.echosite.cn/pay/notify.do");


            } else {
                return null;
            }
        } else {
            return null;
        }


    }

    /***
     * * 查询订单状态
     * * @param orderId
     * * @return
     * */

    @GetMapping("/queryPayStatus")
    public Map<String, String> queryPayStatus(String orderId) {

        Map<String, String> resultMap = weixinPayService.queryPayStatus(orderId);


        //如果支付成功
        if (resultMap.get("return_code").equalsIgnoreCase("success") && resultMap.get("result_code").equalsIgnoreCase("success")) {

            //获取支付状态

            String result = resultMap.get("trade_state");
            if (result.equalsIgnoreCase("success")) {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                //支付成功,修改订单状态
                secKillOrderService.updatePayStatus(resultMap.get("out_trade_no"), resultMap.get("transaction _id"), username);


            }

        }
        return resultMap;
    }
}
