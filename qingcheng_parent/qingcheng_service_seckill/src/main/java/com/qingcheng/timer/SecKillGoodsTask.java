package com.qingcheng.timer;

import com.qingcheng.dao.SeckillGoodsMapper;
import com.qingcheng.pojo.seckill.SeckillGoods;
import com.qingcheng.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SecKillGoodsTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 设置定时方法加载秒杀商品进缓存
     * 0/15:表示从0秒开始执行，每过15秒再次执行
     */
    @Scheduled(cron = "0/15 * * * * ?")
    public void loadGoodsToRedis() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("当前时间:" +simpleDateFormat.format(new Date()));
        //1 获取时间段集合
        List<Date> dateMenus = DateUtil.getDateMenus();
        //2 循环时间区间  查询每个商品对应的秒杀商品
        for (Date startTime : dateMenus) {
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //2.1 商品必须通过
            criteria.andEqualTo("status", "1");
            //2.2 库存>0
            criteria.andGreaterThan("stockCount", 0);
            //2.3 秒杀时间大于等于当前循环的区间的开始时间
            criteria.andGreaterThanOrEqualTo("startTime", startTime);
            //2.4 秒杀结束时间小于当前循环的时间区间的开始时间+2小时
            criteria.andLessThan("endTime", DateUtil.addDateHour(startTime, 2));
            //2.5 过滤Redis中已经存在的该区间的秒杀商品
            Set keys = redisTemplate.boundHashOps("SecKillGoods_" + DateUtil.date2Str(startTime)).keys();
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }

            //2.6 执行查询
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);//数据库对应时间区间所有秒杀商品
            System.out.println(simpleDateFormat.format(startTime)+"共查询数据:"+seckillGoods.size());

            //3.将秒杀商品存入到Redis缓存
            for (SeckillGoods seckillGood : seckillGoods) {     //redis中对应时间区间所有秒杀商品
                //存储商品详情
                redisTemplate.boundHashOps("SecKillGoods_" + DateUtil.date2Str(startTime)).put(seckillGood.getId(), seckillGood);

                //商品数据队列存储,防止高并发超卖
                Long[] ids = pushIds(seckillGood.getStockCount(), seckillGood.getId());
                redisTemplate.boundListOps("SeckillGoodsCountList" + seckillGood.getId()).leftPushAll(ids);

                //创建商品数量
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGood.getId(),seckillGood.getStockCount());

            }

        }
        }


    /**
     * 将所有商品的个数放入缓存 每个都是商品Id
     * @return
     */
    public Long[] pushIds(int len,long id) {
        Long[] ids =new Long[len];
        for (int i = 0; i <ids.length; i++) {
            ids[i]=id;
        }
        return ids;

    }


}
