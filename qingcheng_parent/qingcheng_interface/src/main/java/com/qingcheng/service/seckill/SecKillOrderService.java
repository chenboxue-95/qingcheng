package com.qingcheng.service.seckill;

import com.qingcheng.pojo.seckill.SeckillOrder;
import com.qingcheng.pojo.seckill.SeckillStatus;

/**
 * 下单接口
 */
public interface SecKillOrderService {
    /**
     * 添加秒杀订单
     *
     * @param time     商品时间段
     * @param username 用户名
     * @param id       商品id
     * @return 下单结果
     */
    public boolean add(String time, String username, Long id);


    /**
     * 查看抢单状态
     *
     * @param username
     * @return
     */
    public SeckillStatus queryStatus(String username);

    /***  * 更新订单状态
     * * @param out_trade_no
     * * @param transaction_id
     * * @param username  */
    public void updatePayStatus(String out_trade_no, String transaction_id, String username);

    /**
     * 根据用户名查询订单对象
     * @param username
     * @return
     */
    public SeckillOrder findById(String username);
}
