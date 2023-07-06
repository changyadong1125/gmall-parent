package com.atguigu.gmall.controller.admin;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.service.MangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.productController
 * class:ManageController
 *
 * @author: smile
 * @create: 2023/7/5-10:51
 * @Version: v1.0
 * @Description:
 */
@Api("商品后台管理模块")
@RequestMapping("/admin/product")
@RestController
public class ManageController {
    @Resource
    private MangeService mangeService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取所有一级分类数据
     */
    @GetMapping("/getCategory1")
    @ApiOperation("获取一级分类")
    public Result<List<BaseCategory1>> getCategory1() {
        List<BaseCategory1> BaseCategory1List = mangeService.getCategory1();
        return Result.ok(BaseCategory1List);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据一级分类Id获取二级分类
     */
    @GetMapping("/getCategory2/{category1Id}")
    @ApiOperation("获取二级分类")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable Long category1Id) {
        List<BaseCategory2> baseCategory2List = mangeService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据一级分类Id获取二级分类
     */
    @GetMapping("/getCategory3/{category2Id}")
    @ApiOperation("获取三级分类")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable Long category2Id) {
        List<BaseCategory3> baseCategory3List = mangeService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据分类Id查询平台属性集合
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    @ApiOperation("获取平台属性集合")
    public Result<List<BaseAttrInfo>> getAttrInfoList(@PathVariable Long category1Id,
                                                      @PathVariable Long category2Id,
                                                      @PathVariable Long category3Id) {
        List<BaseAttrInfo> baseAttrInfoList = mangeService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(baseAttrInfoList);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加平台属性
     */
    @PostMapping("/saveAttrInfo")
    public Result<?> saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        mangeService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据属性Id获取平台属性值列表
     */
    @GetMapping("/getAttrValueList/{attrId}")
    public Result<?> getAttrValueList(@PathVariable Long attrId) {
        List<BaseAttrValue> baseAttrValueList = mangeService.getAttrValueList(attrId);
        return Result.ok(baseAttrValueList);
    }
}
