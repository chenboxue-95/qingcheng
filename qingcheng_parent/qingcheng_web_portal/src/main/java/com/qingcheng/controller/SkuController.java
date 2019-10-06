package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.ImportSku;
import com.qingcheng.service.goods.SkuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/sku")
public class SkuController {

    @Reference
    private SkuService skuService;

    @Reference
    private ImportSku importSku;

    @GetMapping("/importSku")
    public void importSku(){
        //查询
        try {
            importSku.importAllSkuList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @GetMapping("/price")
    public Integer price(String id){
        return skuService.findPriceById(id);
    }

}



