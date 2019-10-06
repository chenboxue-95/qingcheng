package com.qingcheng.service.pay;

import java.util.Map;

/**
 * 微信支付接口
 */
public interface WeixinPayService {


    /**
     * 下单生成微信支付二维码
     * @param orderId 订单号
     * @param money 商品金额
     * @param notify 回调地址
     * @return 二维码
     */
    public Map createNative(String orderId,Integer money,String notify);

    /**
     * 微信支付回调 验证签名正确性
     * @param xml 回调xml参数
     */
    public void notifyLogin(String xml);

    /**
     * 根据订单号查询对应订单支付结果
     * @param orderId
     */
   public Map queryPayStatus(String orderId);

    /**
     * 关闭未支付订单
     * @param orderId
     */
   public Map<String,String> closePay(String orderId) throws Exception;
}
