package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.mq.controller
 * class:MqController
 *
 * @author: smile
 * @create: 2023/7/25-9:53
 * @Version: v1.0
 * @Description:
 */
@RestController
@RequestMapping("/mq")
public class MqController {


    @Resource
    private RabbitService rabbitService;


    /**
     * 消息发送
     */
    //http://localhost:8282/mq/sendConfirm
    @GetMapping("sendConfirm")
    public Result<?> sendConfirm() {
        rabbitService.sendMessage("exchange.confirm", "routing.confirm", "来人了，开始接客吧！");
        return Result.ok();
    }
}
