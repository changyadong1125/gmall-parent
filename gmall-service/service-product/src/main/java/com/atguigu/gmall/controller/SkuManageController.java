package com.atguigu.gmall.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.service.SkuManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.controller
 * class:SkuManageController
 *
 * @author: smile
 * @create: 2023/7/8-15:06
 * @Version: v1.0
 * @Description:
 */
@RestController
@RequestMapping("/admin/product")
public class SkuManageController {
    @Resource
    private SkuManageService skuManageService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id查询spu列表
     */
    @GetMapping("/list/{pageNum}/{pageSize}")
    public Result<?> getSkuPage(@PathVariable("pageNum") Long pageNum,
                                @PathVariable("pageSize") Long pageSize,
                                @RequestParam Long category3Id) {
        IPage<SkuInfo> iPage = new Page<>(pageNum, pageSize);
        iPage = skuManageService.getSkuPage(iPage, category3Id);
        return Result.ok(iPage);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存saveSkuInfo
     */
    @PostMapping("/saveSkuInfo")
    public Result<?> saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:回显skuInfo
     */
    @GetMapping("/getSkuInfo/{id}")
    public Result<?> getSkuInfo(@PathVariable Long id) {
        SkuInfo skuInfo = skuManageService.getSkuInfo(id);
        return Result.ok(skuInfo);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改sku
     */
    @PostMapping("/updateSkuInfo")
    public Result<?> updateSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.updateSkuInfo(skuInfo);
        return Result.ok();
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品上架
     */
    @GetMapping("/onSale/{skuId}")
    public Result<?> onSale(@PathVariable("skuId") Long skuId){
        skuManageService.onSale(skuId);
        return Result.ok();
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品下架
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result<?> cancelSale(@PathVariable("skuId") Long skuId){
        skuManageService.cancelSale(skuId);
        return Result.ok();
    }
}

