package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.pay.WeixinPayService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/wxpay")
public class WxPayController {

    @Reference
    private OrderService orderService;

    @Reference
    private WeixinPayService weixinPayService;

    /**
     * 根据订单号生成微信支付地址
     *
     * @param orderId 订单号
     * @return 微信支付地址
     */
    @GetMapping("/createNative")
    public Map createNative(String orderId) {
        // 1 根据登陆用户查询订单
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderService.findById(orderId);
        if (order != null) {
            if ("0".equals(order.getOrderStatus()) && "0".equals(order.getPayStatus()) && username.equals(order.getUsername())) {

                // 2 根据订单调用微信服务层返回微信支付地址
                Map map = weixinPayService.createNative(orderId, order.getPayMoney(), "http://zhoujian.easy.echosite.cn/wxpay/notify.do");
                System.out.println("微信支付地址:"+ map);
                return map;
            } else {
                return null;
            }
        }
        return null;
    }


    //订单号:  1153335740386775040

    @RequestMapping("/notify")
    public void notifyLogin(HttpServletRequest request) {
        System.out.println("支付成功回调.....");
        try {
            // 1 获得微信返回过来的流信息
            InputStream inputStream = request.getInputStream();
            // 2 通过输入流写入
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer =new byte[1024];//微信返回的字节流信息
            int len = 0;
            while ((len=inputStream.read(buffer))!=-1){
                outStream.write(buffer,0,len);
            }
            outStream.close();
            inputStream.close();
            String result = new String(outStream.toByteArray(), "utf-8"); //微信平台  二进制信息转为字符串
//            System.out.println("微信返回结果:"+result);

            weixinPayService.notifyLogin(result);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
