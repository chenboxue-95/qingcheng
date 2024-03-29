package com.qingcheng.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Reference
    private SpuService spuService;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagePath}")
    private String pagePath;

    @Reference
    private CategoryService categoryService;


    /**
     * 生成商品详情页
     */

    //http://localhost:9102/item/createPage.do?spuId=10000015212900
    @RequestMapping("/createPage")
    public void createPage(String spuId) {
        //根据id查询对应商品信息
        Goods goods = spuService.findGoodsById(spuId);
        //获取spu信息
        Spu spu = goods.getSpu();
        //获取sku集合
        List<Sku> skuList = goods.getSkuList();

        //查询商品分类
        List<String> categoryList = new ArrayList<>();
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName());//一级分类
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName());//一级分类
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName());//一级分类

        //sku地址列表
        Map<String,String> urlMap=new HashMap<>();
        for(Sku sku:skuList){
            if("1".equals(sku.getStatus())){
                String specJson = JSON.toJSONString( JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
                urlMap.put(specJson,sku.getId()+".html");
            }
        }



        //创建页面  (为每一个sku创建一个页面)
        for (Sku sku : skuList) {
            // 1 上下文
            Context context = new Context();
            //创建数据模型
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("spu", spu);
            dataModel.put("sku", sku);
            dataModel.put("categoryList", categoryList);  //商品分类面包屑
            dataModel.put("skuImages", sku.getImages().split(","));//sku图片列表
            dataModel.put("spuImages", spu.getImages().split(","));//spu图片列表

            Map paraItems = JSON.parseObject(spu.getParaItems());//spu参数列表
            dataModel.put("paraItems", paraItems);
            Map<String, String> specItems = (Map) JSON.parseObject(sku.getSpec());//sku规格列表
            dataModel.put("specItems", specItems);

            //{"颜色":["天空之境","珠光贝母"],"内存":["8GB+64GB","8GB+128GB","8GB+256GB"]}
            //{"颜色":[{ 'option':'天空之境',checked:true },{ 'option':'珠光贝母',checked:false }],.....}
            Map<String, List> specMap = (Map) JSON.parseObject(spu.getSpecItems());//规格和规格选项
            for (String key : specMap.keySet()) { //循环规格
                List<String> list = specMap.get(key);  //获取对应的值集合: ["天空之境","珠光贝母"]
                List<Map> mapList = new ArrayList<>(); //新的集合  [{ 'option':'天空之境',checked:true },{ 'option':'珠光贝母',checked:false }]
                for (String value : list) {
                    Map map = new HashMap();
                    map.put("option", value);  //单独的规格选项
//                    System.out.println(value);
                    if (specItems.get(key).equals(value)) { // 如果和当前sku的规格相同，就是选中
                        map.put("checked", true);   //是否选中
                    } else {
                        map.put("checked", false);   //是否选中
                    }
                    Map<String,String>  spec= (Map)JSON.parseObject(sku.getSpec()) ;//当前的Sku
                    spec.put(key,value);
                    String specJson = JSON.toJSONString(spec , SerializerFeature.MapSortField);
                    map.put("url",urlMap.get(specJson));
                    mapList.add(map);
                }

                specMap.put(key, mapList);  //用新的集合替换原有集合

            }
                dataModel.put("specMap", specMap);  //规格面板
                context.setVariables(dataModel);

                // 2.准备文件
                File dir = new File(pagePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, sku.getId() + ".html");

                //3 生成页面
                try {
                    PrintWriter writer = new PrintWriter(dest, "UTF-8");
                    templateEngine.process("item", context, writer);
                    System.out.println("生成的页面:" + sku.getId() + ".html");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }




    }
}
