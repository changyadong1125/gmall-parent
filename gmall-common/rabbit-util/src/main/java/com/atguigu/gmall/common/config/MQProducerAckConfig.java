package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import com.sun.deploy.ui.UIFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.common.config
 * class:MQProducerAckConfig
 *
 * @author: smile
 * @create: 2023/7/25-10:21
 * @Version: v1.0
 * @Description:
 */
@Slf4j
@Configuration
public class MQProducerAckConfig implements RabbitTemplate.ReturnCallback, RabbitTemplate.ConfirmCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 应用启动后触发一次
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 只确认消息是否正确到达 Exchange 中,成功与否都会回调
     *
     * @param correlationData 相关数据  非消息本身业务数据
     * @param ack             应答结果
     * @param cause           如果发送消息到交换器失败，错误原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            //消息到交换器成功
            log.info("消息发送到Exchange成功：{}", correlationData);
        } else {
            //消息到交换器失败
            log.error("消息发送到Exchange失败：{},将重试发送消息。", cause);
            //重发消息
            this.retryMessage(correlationData);
        }
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:重试方法
     */
    private void retryMessage(CorrelationData correlationData) {
        //先获取缓存中的数据 缓存中的数据有重试次数
        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;
        //获取重试次数
        int retryCount = gmallCorrelationData.getRetryCount();
        if (retryCount >= 3) {
            log.error("重试以到最大次数:{}", retryCount);
        } else {
            //更新重试次数 写会缓存
            gmallCorrelationData.setRetryCount(++retryCount);
            this.redisTemplate.opsForValue().set(Objects.requireNonNull(correlationData.getId()), JSONObject.toJSONString(gmallCorrelationData), 10, TimeUnit.MINUTES);
            if (gmallCorrelationData.isDelay()) {
                //发送延迟消息
                log.info("重试第{}:次发送》》》》》》》》》...", retryCount);
                this.rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),gmallCorrelationData.getRoutingKey(),message -> {
                            message.getMessageProperties().setDelay(gmallCorrelationData.getDelayTime()*1000);
                            return message;}, gmallCorrelationData);
            } else {
                //重试发消息
                log.info("重试第:{}次发送》》》》》》》》》...", retryCount);
                this.rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(), (Object) gmallCorrelationData.getRoutingKey(), gmallCorrelationData);
            }
        }
    }


    /**
     * 消息没有正确到达队列时触发回调，如果正确到达队列不执行
     *
     * @param message    消息对象
     * @param replyCode  应答码
     * @param replyText  应答提示信息
     * @param exchange   交换器
     * @param routingKey 路由键
     */
    @Override
    @SuppressWarnings("all")
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.error("消息路由queue失败，应答码={}，原因={}，交换机={}，路由键={}，消息={}",
                replyCode, replyText, exchange, routingKey, message.toString());
        //消息没有到队列  也需要重试 从缓存中获取CorrelationData
        String messageId = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        String correlationDataString = this.redisTemplate.opsForValue().get(messageId);
        GmallCorrelationData gmallCorrelationData = JSONObject.parseObject(correlationDataString, GmallCorrelationData.class);
//        if (gmallCorrelationData.isDelay()) {
//            return;
//        }
        this.retryMessage(gmallCorrelationData);
    }
}
