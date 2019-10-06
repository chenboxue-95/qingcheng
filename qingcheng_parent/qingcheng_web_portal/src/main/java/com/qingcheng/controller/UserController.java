package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.user.User;
import com.qingcheng.service.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 获取验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sendSms")
    public Result sendSms(String phone){
        userService.sendSms(phone);
        return new Result();
    }

    /**
     * 注册用户
     * @param user
     * @param smsCode
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody User user, String smsCode){
        System.out.println(user);
        System.out.println(smsCode);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String newPassword = encoder.encode(user.getPassword());
        user.setPassword(newPassword);
        userService.add(user,smsCode);

        return new Result();

    }


}
