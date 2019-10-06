package com.qingcheng.service.Impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.SecKillOrderMapper;
import com.qingcheng.pojo.seckill.SeckillOrder;
import com.qingcheng.service.seckill.SecKillOrderService;
import com.qingcheng.task.MultiThreadingCreateOrder;
import com.qingcheng.pojo.seckill.SeckillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {


    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SecKillOrderMapper seckillOrderMapper;

    /**
     * 添加秒杀订单
     *
     * @param time     商品时间段
     * @param username 用户名
     * @param id       商品id
     * @return
     */
    @Override
    public boolean add(String time, String username, Long id) {


        //先判断是否有库存
        Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + id).size();
        if (size <= 0) {
            //101表示没库存
            throw new RuntimeException("101");
        }
        //其次防止重复排队
        //自增特性
        //  incr(key,value):让指定key的值自增value->返回自增的值->单线程操作
        //  A   第1次:  incr(username,1)->1
        //      第2次:  incr(username,1)->2
        //      利用自增，如果用户多次提交或者多次排队，则递增值>1
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if (userQueueCount > 1) { //重复排队
            System.out.println("重复抢单...");
            // 100 表示重复排队状态码
            throw new RuntimeException("100");
        }


        // 1 创建队列所需排队信息
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);

        //2 排队信息发送至redis队列中,模仿排队场景   [下单队列,用于抢单信息存储]  // 一次性操作
        redisTemplate.boundListOps("SecKillOrderQueue").leftPush(seckillStatus);//list特性 队列取出来就没了,所以无需删除

        //3 用户抢单信息存储  [抢单信息存储,实时获得  {用户队列,用户查看抢单状态]    为了防止前台查询订单状态而查不到不到订单数据,保证总有订单,造成缓存穿透
        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

        multiThreadingCreateOrder.createOrder();

        System.out.println("其他程序正在执行..");

        return true;
    }


    /**
     * 查看抢单状态
     *
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        // 去redis缓存查看订单状态
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);

    }

    @Override
    public void updatePayStatus(String out_trade_no, String transaction_id, String username) {
        //订单数据从Redis数据库查询出来
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //修改状态 1:排队中，2:秒杀等待支付,3:支付超时，4:秒杀失败,5:支付完成
        seckillOrder.setStatus("1");

        //支付时间
        seckillOrder.setPayTime(new Date());

        //同步到MySQL中

        seckillOrderMapper.insertSelective(seckillOrder);

        //清空Redis缓存
        redisTemplate.boundHashOps("SeckillOrder").delete(username);

        //清空用户排队数据
        redisTemplate.boundHashOps("UserQueueCount").delete(username);

        //删除抢购状态信息

        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }

    /**
     * 根据用户名查找订单
     * @param username
     * @return
     */
    @Override
    public SeckillOrder findById(String username) {
        return (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
    }
}
