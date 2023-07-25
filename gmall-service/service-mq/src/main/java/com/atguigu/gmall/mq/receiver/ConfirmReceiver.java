package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;


@Component
public class ConfirmReceiver {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm",durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    @SneakyThrows
    public void getMsg(String msg , Channel channel, Message message){
        //获取消息
        System.out.println("消息：" + msg);
        System.out.println("消息：" + new String(message.getBody()));
        //开启确认 第一个参数表示消息的唯一表示 第二个表示是否批量确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getDeadMsg(String msg , Channel channel, Message message){
        System.out.println("消息：" + msg);
        //开启确认 第一个参数表示消息的唯一表示 第二个表示是否批量确认
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void getDeadMsg2(String msg , Channel channel, Message message){
        System.out.println("消息：" + msg);
        //开启确认 第一个参数表示消息的唯一表示 第二个表示是否批量确认
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
