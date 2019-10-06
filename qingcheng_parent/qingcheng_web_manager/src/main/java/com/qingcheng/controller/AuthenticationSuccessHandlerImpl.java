package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * 登陆成功处理器
 */


public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference
    private LoginLogService loginLogService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("登陆成功处理器到此一游!");
        String loginName=authentication.getName();//当前用户姓名
        String ip = request.getRemoteAddr();//获取ip地址
        String agent = request.getHeader("user-agent");

//        登陆成功添加日志
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginTime(new Date());
        loginLog.setLoginName(loginName);
        loginLog.setIp(ip);//ip
        loginLog.setLocation(WebUtil.getCityByIP(ip));//城市
        loginLog.setBrowserName(WebUtil.getBrowserName(agent));//浏览器名称

        loginLogService.add(loginLog);

        request.getRequestDispatcher("/main.html").forward(request,response);
    }
}
