package com.qingcheng.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.seckill.SeckillGoods;
import com.qingcheng.service.seckill.SecKillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据时间区间查询对应秒杀商品
     *
     * @param time 时间区间
     * @return
     */
    @Override
    public List<SeckillGoods> list(String time) {
        return redisTemplate.boundHashOps("SecKillGoods_" + time).values();
    }

    /**
     * 根据商品ID和时间查询商品详情
     * @param time
     * @param id
     * @return
     */
    @Override
    public SeckillGoods one(String time, Long id) {
        return (SeckillGoods) redisTemplate.boundHashOps("SecKillGoods_" + time).get(id);

    }
}
