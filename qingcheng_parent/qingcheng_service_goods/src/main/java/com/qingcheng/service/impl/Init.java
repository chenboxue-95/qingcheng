package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuService skuService;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("预热缓存");
        categoryService.saveCategoryTreeToRedis();  //加载商品分类导航缓存
        skuService.saveAllPriceToRedis(); //加载价格数据
    }
}
