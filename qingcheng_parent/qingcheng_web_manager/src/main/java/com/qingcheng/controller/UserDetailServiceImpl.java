package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.service.system.ResourceService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证登陆信息
 */
public class UserDetailServiceImpl implements UserDetailsService {

    @Reference
    private AdminService adminService;

    @Reference
    private ResourceService resourceService;

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        System.out.println("经过了UserDetailsService");
        //        查询管理员
        Map map=new HashMap();
        map.put("loginName",loginName);
        map.put("status","1");
        List <Admin> list= adminService.findList(map);
        if (list.size()==0){
            return null;
        }

//        构建角色集合,和security框架从前台获得的数据进行对比  从数据库动态资源获取

        List<GrantedAuthority> grantedAuths =new ArrayList<GrantedAuthority>();
        List<String> resources = resourceService.findResourcesByLoginName(loginName);
        for (String resource : resources) {
            grantedAuths.add(new SimpleGrantedAuthority(resource));
        }

        return new User(loginName,list.get(0).getPassword(),grantedAuths);
    }



}
