package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.ReturnOrder;

import java.util.*;

/**
 * returnOrder业务逻辑层
 */
public interface ReturnOrderService {


    public List<ReturnOrder> findAll();


    public PageResult<ReturnOrder> findPage(int page, int size);


    public List<ReturnOrder> findList(Map<String,Object> searchMap);


    public PageResult<ReturnOrder> findPage(Map<String,Object> searchMap,int page, int size);


    public ReturnOrder findById(Long id);

    public void add(ReturnOrder returnOrder);


    public void update(ReturnOrder returnOrder);


    public void delete(Long id);

//    同意退款
    public void agreeRefund(String id,Integer money,Integer adminId);

//    驳回退款
    public void rejectRefund(String id,String remark,Integer adminId);

}
