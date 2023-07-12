package com.atguigu.gmall.product.controller;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SpuManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.controller
 * class:SpuManageController
 *
 * @author: smile
 * @create: 2023/7/7-19:28
 * @Version: v1.0
 * @Description:
 */
@RestController
@RequestMapping("/admin/product")
public class SpuManageController {
    @Resource
    private SpuManageService spuManageService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id分页查询spu
     */
    @GetMapping("/{pageNum}/{pageSize}")
    public Result<?> getSpuPage(@PathVariable Long pageNum,
                                @PathVariable Long pageSize,
                                @RequestParam Long category3Id) {
        IPage<SpuInfo> iPage = new Page<>(pageNum, pageSize);
        iPage = spuManageService.getSpuPage(iPage, category3Id);
        return Result.ok(iPage);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加spu
     */
    @PostMapping("/saveSpuInfo")
    public Result<?> saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuManageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:回显spu
     */
    @GetMapping("/getSpuInfo/{spuId}")
    public Result<?> getSpuInfo(@PathVariable Long spuId) {
        SpuInfo spuInfo = spuManageService.getSpuInfo(spuId);
        return Result.ok(spuInfo);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改spu
     */
    @PostMapping("/updateSpuInfo")
    public Result<?> updateSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuManageService.updateSpuInfo(spuInfo);
        return Result.ok(spuInfo);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId获取spu销售属性列表
     */
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result<?> spuSaleAttrList(@PathVariable Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuManageService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId获取spu图片列表
     */
    @GetMapping("/spuImageList/{spuId}")
    public Result<?> spuImageList(@PathVariable Long spuId) {
        List<SpuImage> spuImageList = spuManageService.spuImageList(spuId);
        return Result.ok(spuImageList);
    }
}
