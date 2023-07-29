package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.common.service
 * class:RabbitService
 *
 * @author: smile
 * @create: 2023/7/25-11:40
 * @Version: v1.0
 * @Description:
 */
@Service
@SuppressWarnings("all")
public class RabbitService {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
        String messageId = "MessageId:"+UUID.randomUUID().toString();
        gmallCorrelationData.setId(messageId);
        //将这个对象存储到缓存中   将消息发封装到对象中同时将对象放入redis中
        this.redisTemplate.opsForValue().set(messageId, JSONObject.toJSONString(gmallCorrelationData), 10, TimeUnit.MINUTES);
        rabbitTemplate.convertAndSend(exchange, routingKey, message, gmallCorrelationData);
        return true;
    }

    public boolean sendDelayMessage(String exchange, String routingKey, Object obj, int delay) {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        gmallCorrelationData.setMessage(obj);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        String delayMessageId = "DelayMessageId:"+UUID.randomUUID().toString();
        gmallCorrelationData.setId(delayMessageId);
        gmallCorrelationData.setDelayTime(delay);
        //重试次数 初始化为零  超过三则不需要重试
        gmallCorrelationData.setDelay(true);
        //将这个对象存储到缓存中   将消息发封装到对象中同时将对象放入redis中
        this.redisTemplate.opsForValue().set(delayMessageId, JSONObject.toJSONString(gmallCorrelationData), 10, TimeUnit.MINUTES);
        rabbitTemplate.convertAndSend(exchange, routingKey, obj, message -> {
            message.getMessageProperties().setDelay(delay * 1000);
            return message;
        },gmallCorrelationData);
        return true;
    }
}
