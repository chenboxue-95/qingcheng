package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonResponse;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * rabbit监听短信类
 */

public class SmsMessageConsumer implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Value("${smsCode}")
    private String smsCode;

    @Value("${param}")
    private String param;

    @Override
    public void onMessage(Message message) {
        String jsonCode = new String(message.getBody());//获得rabbit验证码的Json字符串
        Map<String,String> map = JSON.parseObject(jsonCode, Map.class);//获得验证码
        String phone = map.get("phone");
        String code = map.get("code");
        System.out.println("手机号"+phone+"  "+"验证码"+code);

        //调用阿里云通信
        CommonResponse commonResponse = smsUtil.sendMs(phone, smsCode, param.replace("[value]", code));


    }
}
