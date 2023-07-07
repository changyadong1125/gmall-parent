package com.atguigu.gmall.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.service.BaseCategoryTrademarkService;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.redis.core.script.ReactiveScriptExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.controller
 * class:BaseCategoryTradeMarkController
 *
 * @author: smile
 * @create: 2023/7/7-15:58
 * @Version: v1.0
 * @Description:
 */
@RestController
@RequestMapping("/admin/product/baseCategoryTrademark")
public class BaseCategoryTradeMarkController {
    @Resource
    private BaseCategoryTrademarkService baseCategoryTrademarkService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id获取品牌列表
     */
    @GetMapping("/findTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> baseTrademarkList = baseCategoryTrademarkService.findTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id获取当前可以添加的所有品牌列表
     */
    @GetMapping("/findCurrentTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findCurrentTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> currentTrademarkList = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(currentTrademarkList);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:新增当前三级分类品牌
     */
    @PostMapping("/save")
    public Result<?> saveTrademark(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        baseCategoryTrademarkService.saveTrademark(categoryTrademarkVo);
        return Result.ok();
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id和品牌Id删除品牌
     */
    @DeleteMapping("/remove/{category3Id}/{id}")
    public Result<?> removeTrademark(@PathVariable Long category3Id,
                                   @PathVariable Long id){
        baseCategoryTrademarkService.removeTrademark(category3Id,id);
        return Result.ok();
    }
}
