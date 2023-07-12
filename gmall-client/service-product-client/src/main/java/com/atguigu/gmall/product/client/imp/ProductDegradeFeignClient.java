package com.atguigu.gmall.product.client.imp;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.client.imp
 * class:ProductDegradeFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-15:41
 * @Version: v1.0
 * @Description:
 */
@Component
public class ProductDegradeFeignClient implements ProductFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId查询SkuInfo和skuImage
     */

    public SkuInfo getSkuInfo(@PathVariable Long skuId) {
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id查询分类数据
     */

    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取商品的最新价格
     */

    public BigDecimal getSkuPrice(@PathVariable Long skuId) {
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取海报数据
     */

    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId) {
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取平台属性和平台属性值
     */

    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId) {
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId,skuId 获取销售属性数据
     */

    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId, @PathVariable Long spuId) {
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     */

    public Map<Object, Object> getSkuValueIdsMap(@PathVariable Long spuId) {
        return null;
    }
}
