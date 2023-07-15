package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.controller
 * class:TestController
 *
 * @author: smile
 * @create: 2023/7/13-14:23
 * @Version: v1.0
 * @Description:
 */
@Api(tags = "测试接口")
@RestController
@RequestMapping("/admin/product/test")
public class TestController {

    @Resource
    private TestService testService;

    @GetMapping("testLock")
    public Result<?> testLock() {
        testService.testLock();
        return Result.ok();
    }
    /**
     * 读数据接口
     * @return
     */
    @GetMapping("/read")
    public Result read(){
        String msg = testService.read();
        return Result.ok(msg);
    }

    /**
     * 写数据接口
     * @return
     */
    @GetMapping("/write")
    public Result write(){
        testService.write();
        return Result.ok("写入数据成功");
    }
}
