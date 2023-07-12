package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import javax.annotation.Resource;
import java.util.Map;

@Controller
public class ItemController {

    @Resource
    private ItemFeignClient itemFeignClient;

    /**
     * 渲染商品详情页面
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String getItem(@PathVariable("skuId") Long skuId, Model model) {
        //调用详情微服务获取渲染详情页所有的数据
        Result<Map<String,Object>> result = itemFeignClient.getItem(skuId);
        model.addAllAttributes(result.getData());
        return "item/item";
    }
}