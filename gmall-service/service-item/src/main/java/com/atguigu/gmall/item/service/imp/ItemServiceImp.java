package com.atguigu.gmall.item.service.imp;

import com.alibaba.fastjson.JSON;
import com.atguigu.com.list.client.ListFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.item.service.imp
 * class:ItemServiceImp
 *
 * @author: smile
 * @create: 2023/7/11-16:29
 * @Version: v1.0
 * @Description:
 */
@Service
@Slf4j
public class ItemServiceImp implements ItemService {
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ListFeignClient listFeignClient;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取商品详情数据
     */
    @Override
    public Map<String, Object> getItem(Long skuId) {
        //布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)) {
            log.error("用户查询商品sku不存在：{}", skuId);
            //查询数据不存在直接返回空对象
            return new HashMap<>();
        }
        //创建map容器
        HashMap<String, Object> map = new HashMap<>();
        //获取是skuInfo
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
        //获取attrList
        CompletableFuture<Void> skuAttrListCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            List<HashMap<String, Object>> skuAttrList = attrList.stream().map(baseAttrInfo -> {
                HashMap<String, Object> map1 = new HashMap<>();
                map1.put("attrName", baseAttrInfo.getAttrName());
                map1.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return map1;
            }).collect(Collectors.toList());
            map.put("skuAttrList", skuAttrList);
        }, threadPoolExecutor);
        //获取categoryView
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            map.put("categoryView", productFeignClient.getCategoryView(skuInfo.getCategory3Id()));
        }, threadPoolExecutor);
        //获取price
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            map.put("price", productFeignClient.getSkuPrice(skuId));
        }, threadPoolExecutor);
        //获取spuPosterList
        CompletableFuture<Void> spuPosterListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            map.put("spuPosterList", productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId()));
        }, threadPoolExecutor);
        //获取spuSaleAttrList
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            map.put("spuSaleAttrList", productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId()));
        }, threadPoolExecutor);
        //获取skuValueIdsMap
        CompletableFuture<Void> skuValueIdsMapCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map<Object, Object> skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String jsonString = JSON.toJSONString(skuValueIdsMap);
            map.put("valuesSkuJson", jsonString);
        }, threadPoolExecutor);
        //更新es中商品的分值
        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        },threadPoolExecutor);
        //多任务组合
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                skuAttrListCompletableFuture,
                categoryViewCompletableFuture,
                priceCompletableFuture,
                spuPosterListCompletableFuture,
                spuSaleAttrListCompletableFuture,
                skuValueIdsMapCompletableFuture,
                incrHotScoreCompletableFuture
        ).join();
        //返回结果
        return map;
    }
}
