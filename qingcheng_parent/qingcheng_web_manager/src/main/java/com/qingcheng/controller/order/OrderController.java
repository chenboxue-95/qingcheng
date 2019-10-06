package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderResults;
import com.qingcheng.service.order.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @GetMapping("/findAll")
    public List<Order> findAll(){
        return orderService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Order> findPage(int page, int size){
        return orderService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Order> findList(@RequestBody Map<String,Object> searchMap){
        return orderService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Order> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  orderService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Order findById(String id){
        return orderService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Order order){
        orderService.add(order);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Order order){
        orderService.update(order);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(String id){
        orderService.delete(id);
        return new Result();
    }

    /**
     * 根据ID返回订单和详情
     * @param id
     * @return
     */
    @GetMapping("/findOrderResultById")
    public OrderResults findOrderResultById(String id){
        OrderResults orderResults = orderService.findOrderResultById(id);
        return orderResults;

    }

    /**
     * 根据订单ID数据查询订单集合
     * 	"consignStatus":"0",
     * 	"ids":[10,1028501748186087424,1028501749566013440]
     * }
     * @return
     */
    @PostMapping("/findOrderListByIds")
    public List<Order> findOrderListByIds(@RequestBody Map<String,Object> searchMap){
        //"ids":[10,1028501748186087424,1028501749566013440]  转换成数组
        String str = (String) searchMap.get("ids");  //[10,1028501748186087424,1028501749566013440]
        //通过fastJson 转换成数组
        String[] ids = JSON.parseObject(str, String[].class);
        List<Order> orders = orderService.findOrderListByIds((String) searchMap.get("consignStatus"), ids);
        return orders;
    }

    /**
     *   根据订单集合  来进行发货处理
     */

    @PostMapping("/batchSend")
    public Result batchSend(@RequestBody List<Order> orders){
        orderService.batchSend(orders);
        return new Result(0,"发货成功!");
    }








}
