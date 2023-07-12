package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "商品详情内部数据接口")
@RestController
@RequestMapping("/api/item")
public class ItemApiController {
    @Resource
    private ItemService itemService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取商品详情数据
     */
    @GetMapping("/{skuId}")
    public Result<Map<String,Object>> getItem(@PathVariable Long skuId){
        Map<String,Object> map = itemService.getItem(skuId);
        return Result.ok(map);
    }
}