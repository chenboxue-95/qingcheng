package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {


    /**
     * 添加销量
     * @param id 商品id
     * @param num 商品数量
     */
    @Update("UPDATE tb_sku SET sale_num=sale_num+#{num} WHERE id=#{id}")
    public void addSaleNum(@Param("id") String id,@Param("num") Integer num);


    /**
     * 扣减库存数量
     * @param id 商品id
     * @param num 商品数量
     */
    @Update("UPDATE tb_sku SET num=num-#{num} WHERE id=#{id}")
    public void deductionStock(@Param("id") String id,@Param("num") Integer num);

}
