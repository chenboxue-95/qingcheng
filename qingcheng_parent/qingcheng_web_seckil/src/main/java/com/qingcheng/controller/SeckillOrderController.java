package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.seckill.SeckillStatus;
import com.qingcheng.service.seckill.SecKillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀视屏看到第18个!!!!!!!!!!切记补课!!!!!!!!!!!!!!!
 */
@RestController
@RequestMapping("/seckill/order")
public class SeckillOrderController {

    @Reference
    private SecKillOrderService secKillOrderService;

    /**
     * 判断用户是否登陆
     */
    @GetMapping("/add")
    public Result add(String time, Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //如果用户没登录，则提醒用户登录
            if ("anonymousUser".equals(username)) {
                return new Result(403, "用户未登陆..");
            }
        try {
            //调用Service下单操作
            boolean b = secKillOrderService.add(time, username, id);
            if (b) {
                return new Result(0, "抢单成功!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //将重复排队错误信息返回出去

            return new Result(2,e.getMessage());
        }

        return new Result(1, "下单失败!");
    }

    @RequestMapping("/query")
    public Result queryStatus(){
        //1 获得登陆用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //2 判断登陆用户是都是匿名用户
        if ("anonymousUser".equals(username)) {
            return new Result(403, "用户未登陆..");
        }
        try {
            //3.1查看排队抢购状态
            SeckillStatus seckillStatus = secKillOrderService.queryStatus(username);
            if (seckillStatus!=null){
                //订单存在
                return new Result(seckillStatus.getStatus(),"抢单状态");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //出现异常
            return new Result(0,e.getMessage());//出现异常
        }
        //订单不存在
        return new Result(404,"无订单信息");


    }


}
