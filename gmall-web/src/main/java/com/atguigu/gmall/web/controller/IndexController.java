package com.atguigu.gmall.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


@Controller
public class IndexController {

    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private TemplateEngine templateEngine;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:渲染首页
     */
    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        Result<List<JSONObject>> result = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", result.getData());
        return "/index/index";
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:生成一个静态化模板页面 将静态页面和静态数据放进nginx
     */
    @GetMapping("/createIndex")
    @ResponseBody
    public Result<?> createIndex(){
        Result<List<JSONObject>> result = this.productFeignClient.getBaseCategoryList();
        Context context = new Context();
        context.setVariable("list",result.getData());
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("D:\\Java\\尚硅谷java_电商\\电商\\index.html");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        templateEngine.process("/index/index",context,fileWriter);
        return Result.ok();
    }
}