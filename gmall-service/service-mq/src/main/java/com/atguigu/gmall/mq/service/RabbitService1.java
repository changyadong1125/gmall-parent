package com.atguigu.gmall.mq.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RabbitService1 {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发送消息
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        gmallCorrelationData.setMessage(message);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        String uuid = UUID.randomUUID().toString();
        gmallCorrelationData.setId(uuid);
        //重试次数 初始化为零  超过三则不需要重试
       /* gmallCorrelationData.setRetryCount();
        gmallCorrelationData.setDelay();*/
        //将这个对象存储到缓存中   将消息发封装到对象中同时将对象放入redis中
        this.redisTemplate.opsForValue().set(uuid, JSONObject.toJSONString(gmallCorrelationData), 10, TimeUnit.SECONDS);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, gmallCorrelationData);
        return true;
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:发送延迟消息
     */
    public boolean sendDelayMessage(String exchange, String routingKey, Object message,int delay) {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        gmallCorrelationData.setMessage(message);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        String uuid = UUID.randomUUID().toString();
        gmallCorrelationData.setId(uuid);
        gmallCorrelationData.setDelayTime(delay);
        //重试次数 初始化为零  超过三则不需要重试
        gmallCorrelationData.setDelay(true);
        //将这个对象存储到缓存中   将消息发封装到对象中同时将对象放入redis中
        this.redisTemplate.opsForValue().set(uuid, JSONObject.toJSONString(gmallCorrelationData), 10, TimeUnit.SECONDS);
        rabbitTemplate.convertAndSend(exchange, routingKey, message,message1 -> {
            message1.getMessageProperties().setDelay(delay*1000);
            return message1;
        },gmallCorrelationData);
        return true;
    }

}