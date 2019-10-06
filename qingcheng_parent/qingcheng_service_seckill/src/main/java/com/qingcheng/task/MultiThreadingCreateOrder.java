package com.qingcheng.task;

import com.qingcheng.dao.SeckillGoodsMapper;
import com.qingcheng.pojo.seckill.SeckillGoods;
import com.qingcheng.pojo.seckill.SeckillOrder;
import com.qingcheng.util.IdWorker;
import com.qingcheng.pojo.seckill.SeckillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 多线程下单操作
     */
    @Async
    public void createOrder() {
        System.out.println("准备下单......");
        try {
            Thread.sleep(10000);
            // 0.从redis队列中获取排队信息
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SecKillOrderQueue").rightPop();
            if (seckillStatus != null) {

                Long id = seckillStatus.getGoodsId();
                String username = seckillStatus.getUsername();
                String time = seckillStatus.getTime();
                // 从商品数组中获得商品 判断是否有库存
                Object sgoods = redisTemplate.boundListOps("SeckillGoodsCountList" + id).rightPop();
                if (sgoods == null) { //商品售空 清理排队信息
                    cleanQueue(seckillStatus);
                    return;

                }



                //1 查询redis商品
                SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SecKillGoods_" + time).get(id);
                if (goods != null || goods.getStockCount() >= 1) {  //查看库存
                    //2.1  创建订单
                    SeckillOrder seckillOrder = new SeckillOrder();
                    seckillOrder.setId(idWorker.nextId());
                    seckillOrder.setSeckillId(id);
                    seckillOrder.setMoney(goods.getCostPrice());
                    seckillOrder.setUserId(username);
                    seckillOrder.setSellerId(goods.getSellerId());
                    seckillOrder.setCreateTime(new Date());
                    seckillOrder.setStatus("0");
                    //2.2 添加订单到redis
                    redisTemplate.boundHashOps("SecKillOrder").put(username, seckillOrder);

                    //2.3 库存递减
                    Long seckillGoodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillOrder.getId(), -1);
                    goods.setStockCount(seckillGoodsCount.intValue());

                    //3 判断商品是否还有库存
                    if (seckillGoodsCount <= 0) {
                        // 3.1如果没有缓存 更新数据库  并且删除redis数据
                        seckillGoodsMapper.updateByPrimaryKeySelective(goods);
                        redisTemplate.boundHashOps("SecKillGoods_" + time).delete(id);
                        throw new RuntimeException("商品数量不足");

                    } else { // 3.1如果还有数据  更新redis
                        redisTemplate.boundHashOps("SecKillGoods_" + time).put(id, goods);
                    }

                    // 4 更改抢单状态为已下单
                    seckillStatus.setStatus(2);  //抢单状态为抢单成功 ,待支付
                    seckillStatus.setMoney(seckillOrder.getMoney().floatValue());//设置订单价格
                    seckillStatus.setOrderId(seckillStatus.getOrderId());//设置订单号

                    redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
                    System.out.println("开始下单,准备支付...");

                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    /**
     * 清理排队信息
     * @param seckillStatus
     */
    public void cleanQueue(SeckillStatus seckillStatus) {
        //清理排队标识
        redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());
        //清理抢单标识
        redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());

        //SecKillOrderQueue用户信息队列为何不删??  list特性 队列取出来就没了,所以无需删除

    }


}
