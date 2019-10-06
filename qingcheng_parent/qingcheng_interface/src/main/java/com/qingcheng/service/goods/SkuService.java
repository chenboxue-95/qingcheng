package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;

import java.util.*;

/**
 * sku业务逻辑层
 */
public interface SkuService {


    public List<Sku> findAll();


    public PageResult<Sku> findPage(int page, int size);


    public List<Sku> findList(Map<String,Object> searchMap);


    public PageResult<Sku> findPage(Map<String,Object> searchMap,int page, int size);


    public Sku findById(String id);

    public void add(Sku sku);


    public void update(Sku sku);


    public void delete(String id);

    /**
     * 加载所有价格到缓存
     */
    public void saveAllPriceToRedis();

    /**
     * 根据id查询价格
     */
    public Integer findPriceById(String id);

    /**
     * 保存价格到缓存
     */
    public void savePriceToRedisById(String id,Integer price);

    /**
     * 根据skuId删除价格缓存
     */

    public void deletePriceFromRedis(String id);

    /**
     * 根据购物车扣减库存
     * @param orderItemList 下单购物车
     * @return 执行结果
     */
    public boolean deductionStock(List<OrderItem> orderItemList);
}
