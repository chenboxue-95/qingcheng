package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SearchController {

    @Reference
    private SkuSearchService skuSearchService;

    @GetMapping("/search")
    public String search(Model model, @RequestParam Map<String,String> searchMap) throws Exception {
//        指定字符集
        searchMap= WebUtil.convertCharsetToUTF8(searchMap);

        //设置pageNo页码
        if(searchMap.get("pageNo")==null){
            searchMap.put("pageNo","1");
        }

        //页面传递给后端排序参数
       if(searchMap.get("sort")==null){
           searchMap.put("sort","");
       };
        if(searchMap.get("sortOrder")==null){
            searchMap.put("sortOrder","DESC");
        };


        //修改pageNo为int类型
        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        model.addAttribute("pageNo",pageNo);

        //返回结果集
        Map result = skuSearchService.search(searchMap);
        model.addAttribute("result",result);

        //页码数量控制
        Long totalPages = (Long)result.get("totalPages");//获取总页数
        int startPage =1; //开始页码
        int endPage =totalPages.intValue();  //结束页码
        if(totalPages>5){
            startPage=pageNo-2;
            if(startPage<1){
                startPage=1;
            }
            endPage=startPage+4;
        }

        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);

        //url处理
        StringBuffer url =new StringBuffer("/search.do?");
        for (String key : searchMap.keySet()) {
            url.append("&"+key+"="+searchMap.get(key));
        }
        model.addAttribute("url",url);

        model.addAttribute("searchMap",searchMap);

        return "search";
    }

}
