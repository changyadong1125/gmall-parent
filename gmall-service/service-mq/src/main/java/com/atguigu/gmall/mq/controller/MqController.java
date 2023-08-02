package com.atguigu.gmall.mq.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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

    @GetMapping("sendConfirm")
    public Result<?> sendConfirm() {
        rabbitService.sendMessage("exchange.confirm", "routing.confirm11", "来人了，开始接客吧！");
        return Result.ok();
    }

    @GetMapping("/sendDeadLetterMsg")
    public Result<?> sendDeadLetterMsg() {
        rabbitService.sendMessage(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, "今天是个好日子！");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));
        return Result.ok();
    }

    @GetMapping("/sendDelayMsg")
    public Result<?> sendDelayMsg() {
        rabbitService.sendDelayMessage(DelayedMqConfig.exchange_delay,
                DelayedMqConfig.routing_delay,
                "基于延迟插件-我是延迟消息",
                10
        );
        return Result.ok();
    }
    //  发送延迟消息-插件实现
    @GetMapping("/sendDelayMsg1")
    public Result<?> sendDelayMsg1(){
        //  每次发送的消息，都给它配一个uuid
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("messageId", UUID.randomUUID().toString());
        jsonObject.put("content", "atguigu");
        //  10s 延迟消息。
        rabbitService.sendDelayMessage(DelayedMqConfig.exchange_delay,DelayedMqConfig.routing_delay,jsonObject,10);
        return Result.ok();
    }
}
