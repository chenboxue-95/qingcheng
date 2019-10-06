package com.qingcheng.service.seckill;

import com.qingcheng.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SecKillGoodsService {

    /**
     * 根据时间区间查询对应秒杀商品
     * @param time 时间区间
     * @return
     */
    public List<SeckillGoods> list(String time);

    /**
     * 根据商品ID和时间查询商品详情
     * @param time
     * @param id
     * @return
     */
    public SeckillGoods one(String time,Long id);
}
