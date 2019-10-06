package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.user.Address;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.user.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private AddressService addressService;

    @Reference
    private OrderService orderService;

    /**
     * 从Redis中提取缓存
     * @return 购物车
     */
    @GetMapping("/findCartList")
    public List<Map<String,Object>> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, Object>> cartList = cartService.findCartList(username);
        return cartList;
    }

    /**
     * 添加商品到购物车
     * @param skuId 商品Id
     * @param num 数量
     * @return 添加结果
     */
    @GetMapping("/addItem")
    public Result addItem(String skuId, Integer num){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(username,skuId,num);
        return new Result();
    }

    /**
     * 与商品详细页对接
     * @param response 重定向对象
     * @param skuId 商品Id
     * @param num 数量
     * @return 结果
     */
    @GetMapping("/buy")
    public void buy(HttpServletResponse response,String skuId, Integer num) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(username,skuId,num);
        response.sendRedirect("/cart.html");

    }

    /**
     * 更新选中状态
     * @param skuId 商品Id
     * @param checked  选中状态
     * @return
     */
    @GetMapping("/updateChecked")
    public Result updateChecked(String skuId, boolean checked) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateChecked(username,skuId,checked);
        return new Result();
    }

    /**
     * 删除选中
     */
    @GetMapping("/deleteCheckedCart")
    public Result deleteCheckedCart(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.deleteCheckedCart(username);
        return new Result();


    }

    /**
     * @return 计算当前购物车优惠金额
     */
    @GetMapping("/preferential")
    public Map preferential(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        int preferential = cartService.preferential(username);
        Map map =new HashMap();
        map.put("preferential",preferential);
        return map;

    }


    /**
     * 获得最新购物车列表
     * @return 购物车列表
     */
    @GetMapping("/findNewOrderItemList")
    public List<Map<String,Object>> findNewOrderItemList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, Object>> newOrderItemList = cartService.findNewOrderItemList(username);
        System.out.println(newOrderItemList);
        return newOrderItemList;
    }

    /**
     * 根据用户名查询地址
     * @return 地址
     */
    @GetMapping("/findAddressList")
    public List<Address> findAddressList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findByUsername(username);
    }

    /**
     * 保存 订单
     * @param order 订单
     * @return 
     */
    @PostMapping("/saveOrder")
    public Map<String,Object> saveOrder(@RequestBody Order order){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUsername(username);
       return orderService.add(order);
    }


}
