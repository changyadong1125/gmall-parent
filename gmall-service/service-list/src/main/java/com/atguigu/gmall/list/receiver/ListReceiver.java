package com.atguigu.gmall.list.receiver;


import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.list.service.SearchService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;


@Component
@Slf4j
public class ListReceiver {
    @Resource
    private SearchService searchService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:上架
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_UPPER}
    ))
    public void upperGoods(Long skuId, Channel channel, Message message) throws IOException {
        try {
            if (skuId != null) {
                searchService.upperGoods(skuId);
            }
        } catch (Exception e) {
            log.error("商品上架失败:{}",skuId);
            throw new RuntimeException(e);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品下架
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_GOODS_LOWER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
            key = {MqConst.ROUTING_GOODS_LOWER}
    ))
    public void lowerGoods(Long skuId, Channel channel, Message message) throws IOException {
        try {
            if (skuId != null) {
                searchService.lowerGoods(skuId);
            }
        } catch (Exception e) {
            log.error("商品上架失败:{}",skuId);
            throw new RuntimeException(e);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}