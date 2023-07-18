package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.list
 * class:controller
 *
 * @author: smile
 * @create: 2023/7/17-14:39
 * @Version: v1.0
 * @Description:
 */
@RestController
@RequestMapping("/api/list")
@RefreshScope
public class ListApiController {
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private SearchService searchService;
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:创建商品索引库
     */
    @GetMapping("/inner/createIndex")
    public Result<?> createIndex() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品上架 传到es
     */
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result<?> upperGoods(@PathVariable("skuId") Long skuId){
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品从es中删除
     */
    @GetMapping("/inner/lowerGoods/{skuId}")
    public Result<?> lowerGoods(@PathVariable("skuId") Long skuId){
        searchService.lowerGoods(skuId);
        return Result.ok();
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新商品的热度排名分值
     */
    @GetMapping("/inner/incrHotScore/{skuId}")
    public Result<?> incrHotScore(@PathVariable("skuId") Long skuId){
        searchService.incrHotScore(skuId);
        return Result.ok();
    }
}
