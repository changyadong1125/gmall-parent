package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.list.service.imp
 * class:SearchServiceImp
 *
 * @author: smile
 * @create: 2023/7/17-15:19
 * @Version: v1.0
 * @Description:
 */
@Service
@Slf4j
public class SearchServiceImp implements SearchService {
    @Resource
    private GoodsRepository goodsRepository;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品上架 传到es
     */
    @Override
    public void upperGoods(Long skuId) {
        //布隆过滤器中查看是否有该商品
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)) {
            log.error("用户查询商品sku不存在：{}", skuId);
            //查询数据不存在直接返回空对象
            throw new GmallException("该商品不存在", 500);
        }
        //创建对象
        Goods goods = new Goods();
        goods.setId(skuId);
        //获取img
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync
                (() -> productFeignClient.getSkuInfo(skuId), threadPoolExecutor);

        CompletableFuture<Void> otherCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setCreateTime(new Date());
        }, threadPoolExecutor);
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
        }, threadPoolExecutor);
        CompletableFuture<Void> trademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseTrademark trademark = this.productFeignClient.getTrademarkById(skuInfo.getTmId());
            //获取品牌数据
            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());
        }, threadPoolExecutor);
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory3Name(categoryView.getCategory3Name());
        }, threadPoolExecutor);
        CompletableFuture<Void> SearchAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(searchAttrList);
        }, threadPoolExecutor);
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                otherCompletableFuture,
                priceCompletableFuture,
                trademarkCompletableFuture,
                categoryViewCompletableFuture,
                SearchAttrCompletableFuture
        ).join();
        goodsRepository.save(goods);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品从es中删除
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新商品的热度排名分值
     */
    @Override
    public void incrHotScore(Long skuId) {
        String key = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        if (score != null && score % 10 == 0) {
            Optional<Goods> optional = goodsRepository.findById(skuId);
            if (optional.isPresent()) {
                Goods goods = optional.get();
                goods.setHotScore(score.longValue());
                //更新es中的数据
                goodsRepository.save(goods);
            }
        }
    }
}
