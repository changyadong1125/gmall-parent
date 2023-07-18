package com.atguigu.gmall.product.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.MangeService;
import com.atguigu.gmall.product.service.SkuManageService;
import com.atguigu.gmall.product.service.SpuManageService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.api
 * class:ProductApiController
 *
 * @author: smile
 * @create: 2023/7/10-15:19
 * @Version: v1.0
 * @Description:
 */
@RestController
@Api("内部数据接口")
@RequestMapping("/api/product/inner")
public class ProductApiController {
    @Resource
    private MangeService mangeService;
    @Resource
    private SkuManageService skuManageService;
    @Resource
    private SpuManageService spuManageService;
    @Resource
    private BaseTrademarkService baseTrademarkService;


    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId查询SkuInfo和skuImage
     */
    @GetMapping("/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId) {
        return mangeService.getSkuInfo(skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id查询分类数据
     */
    @GetMapping("/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {
        return mangeService.getCategoryView(category3Id);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取商品的最新价格
     */
    @GetMapping("/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId) {
        return skuManageService.getSkuPrice(skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取海报数据
     */
    @GetMapping("/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId) {
        return spuManageService.findSpuPosterBySpuId(spuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取平台属性和平台属性值
     */
    @GetMapping("/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId) {
        return skuManageService.getAttrList(skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId,skuId 获取销售属性数据
     */
    @GetMapping("/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId) {
        return spuManageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     */
    @GetMapping("/getSkuValueIdsMap/{spuId}")
    public Map<Object, Object> getSkuValueIdsMap(@PathVariable Long spuId) {
        return skuManageService.getSkuValueIdsMap(spuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取首页分类数据
     */
    @GetMapping("/getBaseCategoryList")
    public Result<List<JSONObject>> getBaseCategoryList(){
        return Result.ok(this.mangeService.getBaseCategoryList());
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据品牌Id查询品牌信息
     */
    @GetMapping("/inner/getTrademark/{tmId}")
    public BaseTrademark getTrademarkById(@PathVariable("tmId") Long tmId) {
        return baseTrademarkService.getTrademarkById(tmId);
    }
}
