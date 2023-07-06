package com.atguigu.gmall.controller;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.controller
 * class:BaseTrademarkController
 *
 * @author: smile
 * @create: 2023/7/6-11:15
 * @Version: v1.0
 * @Description:
 */
@Api(tags = "品牌表控制器")
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {
    @Resource
    private BaseTrademarkService baseTrademarkService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:品牌列表分页查询
     */
    @GetMapping("/{pageNum}/{pageSize}")
    @ApiOperation("品牌列表分页")
    public Result<IPage<BaseTrademark>> getBaseTrademarkPage(@PathVariable Integer pageNum,
                                                             @PathVariable Integer pageSize) {

        IPage<BaseTrademark> baseTrademarkPage = baseTrademarkService.getBaseTrademarkPage(pageNum, pageSize);
        return Result.ok(baseTrademarkPage);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:新增品牌
     */
    @PostMapping("/save")
    public Result<?> saveBaseTrademark(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:回显品牌信息
     */
    @GetMapping("/get/{id}")
    public Result<BaseTrademark> getBaseTrademark(@PathVariable Long id) {
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改品牌信息
     */
    @PutMapping("/update")
    public Result<BaseTrademark> updateBaseTrademark(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:删除品牌
     */
    @DeleteMapping("/remove/{id}")
    public Result<BaseTrademark> deleteBaseTrademark(@PathVariable Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
}
