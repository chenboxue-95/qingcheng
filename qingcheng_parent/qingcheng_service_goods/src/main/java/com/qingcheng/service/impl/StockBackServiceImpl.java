package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.dao.StockBackMapper;
import com.qingcheng.pojo.goods.StockBack;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service(interfaceClass = StockBackService.class)
public class StockBackServiceImpl implements StockBackService {

    /**
     * 获取需要回滚订单,记录回滚日志
     * @param orderItems 需要回滚订单
     */

    @Autowired
    private StockBackMapper stockBackMapper;

    @Autowired
    private SkuMapper skuMapper;


    @Override
    @Transactional
    public void addList(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            StockBack stockBack= new StockBack();
            stockBack.setSkuId(orderItem.getSkuId());
            stockBack.setOrderId(orderItem.getOrderId());
            stockBack.setStatus("0");  //回滚状态: 0:未回滚  1:已回滚
            stockBack.setCreateTime(new Date());  //创建时间
            stockBack.setNum(orderItem.getNum());//商品数量
            stockBackMapper.insert(stockBack);

        }


    }

    /**
     * 回滚
     */
    @Override
    @Transactional
    public void back() {
        //查询回滚订单
        System.out.println("回滚开始...");
        StockBack stockBack = new StockBack();
        stockBack.setStatus("0");
        List<StockBack> stockBacks = stockBackMapper.select(stockBack);

        //进行回滚操作
        for (StockBack stock : stockBacks) {
            //调用增减库存方法  回滚
            skuMapper.deductionStock(stock.getSkuId(),-stock.getNum());//添加库存
            skuMapper.addSaleNum(stock.getSkuId(),-stock.getNum());//减少销量

            stock.setStatus("1");  //设置已回滚
            stock.setBakeTime(new Date());//设置回滚时间
        }

        System.out.println("回滚结束...");
    }
}
