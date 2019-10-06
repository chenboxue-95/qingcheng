package com.qingcheng.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 跳转页面
 */
@Controller
@RequestMapping("/redirect")
public class RedirectController {

    @RequestMapping("/back")
    public String back(@RequestHeader(value = "referer",required = false)String referer){
        if(referer!=null){//来源路径不为空  已经经过CAS登陆
            return "redirect:"+referer;  //
        }else {
            return "/seckill-index.html";//为空 跳转到首页
        }
    }
}
