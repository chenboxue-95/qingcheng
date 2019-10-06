package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.seckill.SeckillGoods;
import com.qingcheng.service.seckill.SecKillGoodsService;
import com.qingcheng.util.DateUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/seckill/goods")
public class SeckillGoodsController {

    @Reference
    private SecKillGoodsService secKillGoodsService;


    /**
     * 返回所有时间菜单
     * @return
     */
    @RequestMapping("/menus")
    public List<Date> loadMenus(){
        return DateUtil.getDateMenus();
    }

    /**
     * 查询所有秒杀商品详情
     * @return
     */
    @GetMapping("/list")
    public List<SeckillGoods> list(String time){
        List<SeckillGoods> list = secKillGoodsService.list(time);
        System.out.println(list);
        return list;
    }

    /**
     * 根据商品ID和时间查询商品详情
     * @param time
     * @param id
     * @return
     */
    @GetMapping("/one")
    public SeckillGoods one(String time, Long id){
        SeckillGoods goods = secKillGoodsService.one(time, id);
        return goods;
    }


}
