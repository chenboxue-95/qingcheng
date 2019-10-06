package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.service.goods.ImportSku;
import com.qingcheng.service.goods.SkuService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ImportSkuImpl  implements ImportSku {

    @Autowired
    private SkuService skuService;


    /**
     * 执行所有的商品导入功能
     */
    @Override
    public void importAllSkuList() throws IOException {
        //1.连接rest接口
        HttpHost http=new HttpHost("127.0.0.1",9200,"http");
        RestClientBuilder restClientBuilder = RestClient.builder(http);
        RestHighLevelClient restHighLevelClient=new RestHighLevelClient(restClientBuilder);

        //2.封装请求对象
        BulkRequest bulkRequest  = new BulkRequest();
        //可以通过循环的方式获得所有sku然后封装成es需要的map对象
        List<Sku> skuList = skuService.findAll();
        for(Sku sku:skuList){
            //把SKU转换成一个map
            IndexRequest indexRequest=new IndexRequest("sku","doc",sku.getId());
            Map skuMap=new HashMap();
            skuMap.put("name",sku.getName());
            skuMap.put("brandName",sku.getBrandName());
            skuMap.put("categoryName",sku.getCategoryName());
            skuMap.put("image",sku.getImage());
            skuMap.put("price",sku.getPrice());
            skuMap.put("createTime",sku.getCreateTime());
            skuMap.put("saleNum",sku.getSaleNum());
            skuMap.put("commentNum",sku.getCommentNum());
            Map map = JSON.parseObject(sku.getSpec(), Map.class);//规格对象map
            skuMap.put("spec",map);
            indexRequest.source(skuMap);
            bulkRequest.add(indexRequest);
        }

        //restHighLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT); // 同步
        //改为异步执行
        //3 异步调用方式
        restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT,new ActionListener<BulkResponse>() {

            public void onResponse(BulkResponse bulkResponse) {
                //成功
                System.out.println("导入成功"+bulkResponse.status());
            }
            public void onFailure(Exception e) {
                //失败
                System.out.println("导入失败"+e.getMessage());
            }
        });
        System.out.println("调用完成");

    }


}
