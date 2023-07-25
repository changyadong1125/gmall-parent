package com.atguigu.gmall.mq.receiver;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class ConfirmReceiver {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    public void getMsg(String msg , Channel channel, Message message) throws IOException {
        //获取消息
        System.out.println("消息：" + msg);
        System.out.println("消息：" + new String(message.getBody()));
        //开启确认 第一个参数表示消息的唯一表示 第二个表示是否批量确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
