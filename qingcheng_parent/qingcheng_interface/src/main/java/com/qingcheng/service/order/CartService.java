package com.qingcheng.service.order;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务
 */
public interface CartService {

    /**
     * 根据登录人姓名去redis提取购物车
     * @param username 登录人
     * @return 购物车列表
     */
    public List<Map<String,Object>> findCartList(String username);


    /**
     * 添加商品到购物车
     * @param username 用户名
     * @param skuId 商品skuId
     * @param num 商品数量
     */
    public void addItem(String username,String skuId,Integer num);

    /**
     * 更新选中状态
     * @param username 用户名
     * @param skuId 商品skuId
     * @param checked  选中状态
     * @return 执行是否成功
     */
    public boolean updateChecked(String username,String skuId,boolean checked);


    /**
     * 删除选中购物车
     * @param username 用户名
     */
    public void deleteCheckedCart(String username);

    /**
     * 计算当前购物车的优惠金额
     * @param username 用户名
     * @return 优惠金额
     */
    public int preferential(String username);

    /**
     * 获得最新购物车列表
     * @param username 用户名
     * @return 购物车列表
     */
    public List<Map<String,Object>> findNewOrderItemList(String username);
}
