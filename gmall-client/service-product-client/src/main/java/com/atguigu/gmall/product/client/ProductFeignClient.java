package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.imp.ProductDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.client
 * class:ProductFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-15:40
 * @Version: v1.0
 * @Description:
 */
@FeignClient(value = "service-product",path = "/api/product/inner",fallback = ProductDegradeFeignClient.class )
public interface ProductFeignClient {

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId查询SkuInfo和skuImage
     */
    @GetMapping("/getSkuInfo/{skuId}")
     SkuInfo getSkuInfo(@PathVariable Long skuId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id查询分类数据
     */
    @GetMapping("/getCategoryView/{category3Id}")
     BaseCategoryView getCategoryView(@PathVariable Long category3Id);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取商品的最新价格
     */
    @GetMapping("/getSkuPrice/{skuId}")
     BigDecimal getSkuPrice(@PathVariable Long skuId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取海报数据
     */
    @GetMapping("/findSpuPosterBySpuId/{spuId}")
     List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取平台属性和平台属性值
     */
    @GetMapping("/getAttrList/{skuId}")
     List<BaseAttrInfo> getAttrList(@PathVariable Long skuId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId,skuId 获取销售属性数据
     */
    @GetMapping("/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
     List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,@PathVariable Long spuId) ;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     */
    @GetMapping("/getSkuValueIdsMap/{spuId}")
     Map<Object, Object> getSkuValueIdsMap(@PathVariable Long spuId) ;
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取首页分类数据
     */
    @GetMapping("/getBaseCategoryList")
     Result<List<JSONObject>> getBaseCategoryList();

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据品牌Id获取品牌信息
     */
    @GetMapping("/inner/getTrademark/{tmId}")
     BaseTrademark getTrademarkById(@PathVariable("tmId") Long tmId);
}
