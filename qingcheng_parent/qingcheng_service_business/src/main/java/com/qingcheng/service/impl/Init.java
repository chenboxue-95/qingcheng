package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {
    @Autowired
    private AdService adService;



    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("轮播图缓存预热");
        //轮播图缓存预热
        adService.saveAllAdToRedis();

    }
}
