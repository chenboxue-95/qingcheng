package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.Config;
import com.github.wxpay.sdk.WXPayRequest;
import com.github.wxpay.sdk.WXPayUtil;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.pay.WeixinPayService;
import com.qingcheng.util.HttpClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinServiceImpl implements WeixinPayService {

    @Autowired
    private Config config;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 生成微信支付二维码
     *
     * @param orderId 订单号
     * @param money   商品金额
     * @param notify  回调地址
     * @return 二维码
     */
    @Override
    public Map createNative(String orderId, Integer money, String notify) {
        try {
            //1 封装请求参数
            Map<String, String> map = new HashMap();
            map.put("appid", config.getAppID());  //公众账号ID
            map.put("mch_id", config.getMchID()); //商户号
            map.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串
            map.put("body", "青橙"); //商品描述
            map.put("out_trade_no", orderId); //商户订单号
            map.put("total_fee", money + ""); //标价金额
            map.put("spbill_create_ip", "127.0.0.1"); //终端IP
            map.put("notify_url", notify); //(通知)回掉地址
            map.put("trade_type", "NATIVE");//交易类型

            String xmlParam = WXPayUtil.generateSignedXml(map, config.getKey());//参数  转为xml格式的参数
//            System.out.println("参数 " + xmlParam);

            //2 发送请求
            WXPayRequest wxPayRequest = new WXPayRequest(config);
            String xmlResult = wxPayRequest.requestWithCert("/pay/unifiedorder", null, xmlParam, false);
//            System.out.println("发送结果: " + xmlResult);


            //3 解析返回结果
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult); //xml形式转为map
//            System.out.println("返回结果1  :"+mapResult);
            boolean signatureValid = WXPayUtil.isSignatureValid(mapResult, config.getKey()); //签名
//            System.out.println("返回结果签名: "+signatureValid);
            String resultCode = mapResult.get("result_code");//状态码
//            System.out.println("返回结果状态码: "+resultCode);
            Map m = new HashMap();
            m.put("code_url", mapResult.get("code_url"));// 支付地址
            m.put("total_fee", money + "");//支付金额
            m.put("out_trade_no", orderId);//订单号
            return m;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }

    /**
     * 微信支付回调 验证签名正确性
     *
     * @param xml 回调xml参数
     */
    @Override
    public void notifyLogin(String xml) {
        //1 将回调xml Sting字符串转为Map
        try {
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
//            System.out.println("返回结果2  :"+map);
            // 2 验证 签名
            boolean flag = WXPayUtil.isSignatureValid(map, config.getKey());
            System.out.println("验证签名是否正确:" + flag);
            System.out.println("订单号:" + map.get("out_trade_no"));
            System.out.println("执行" + map.get("result_code"));

            // 3.1 验证签名结果,
            if (flag) {
                if ("SUCCESS".equals(map.get("result_code"))) {
                    // 3.2 修改订单状态
                    orderService.updatePayStatus(map.get("out_trade_no"), map.get("transaction_id"));
                    //3.3 发送订单给MQ
                    rabbitTemplate.convertAndSend("paynotify", "", map.get("out_trade_no"));
//                    System.out.println("rabbit信息发送成功");
                }
            } else {
                //记录日志
                System.out.println("签名验证有误...");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 根据订单号查询对应订单支付结果
     *
     * @param orderId
     * @return
     */
    @Override
    public Map queryPayStatus(String orderId) {
        Map param = new HashMap();
        param.put("appid", config.getAppID());//公众账号ID
        param.put("mch_id", config.getMchID());//商户号
        param.put("out_trade_no", orderId);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, config.getKey());
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 关闭未支付订单
     *
     * @param orderId
     */
    @Override
    public Map<String, String> closePay(String orderId) throws Exception {
        Map param = new HashMap();
        param.put("appid", config.getAppID());//公众账号ID
        param.put("mch_id", config.getMchID());//商户号
        param.put("out_trade_no", orderId);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";

            String xmlParam = WXPayUtil.generateSignedXml(param, config.getKey());
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            //发送请求
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;

        }
    }

