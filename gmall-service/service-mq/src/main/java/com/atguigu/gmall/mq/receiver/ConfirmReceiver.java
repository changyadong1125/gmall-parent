package com.atguigu.gmall.mq.receiver;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;


@Component
public class ConfirmReceiver {
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm", durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    @SneakyThrows
    public void getMsg(String msg, Channel channel, Message message) {
        try {
            //获取消息
            System.out.println("消息：" + msg);
            System.out.println("消息：" + new String(message.getBody()));
        } catch (Exception e) {
            //如果出现异常怎么办 可以重试消费 最后一个参数表示重回队列 如果出现是网络异常可以行的通
            //借助redis记录当前重回队列的次数  如果重回五次还没有被消费 则记录到消费记录表
            //insert into msg();后续人工处理
            //if (count>=5){ channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);}
            // else{ channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);insert into msg()}
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            e.printStackTrace();
        }
        //开启确认 第一个参数表示消息的唯一表示 第二个表示是否批量确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getDeadMsg(String msg, Channel channel, Message message) {
        System.out.println("消息：" + msg);
        //开启确认 第一个参数表示消息的唯一表示 第二个表示是否批量确认
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void getDeadMsg1(String mes , Channel channel, Message message) {
        System.out.println("消息：" + mes);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    @SneakyThrows
    public void getDeadMsg2(JSONObject jsonObject , Channel channel, Message message) {
        //String messageId = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        // setnx
        String messageId = (String) jsonObject.get("messageId");
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(messageId, messageId);
        if (Boolean.TRUE.equals(aBoolean)){
            try {
                //发生异常
                //int a = 10/0;
                System.out.println("消息：" + jsonObject.get("content"));
                //开启确认 第一个参数表示消息的唯一表示 第二个表示是否批量确认
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println(simpleDateFormat.format(new Date()));
            } catch (Exception e) {
                this.redisTemplate.delete(messageId);
                throw new RuntimeException(e);
            }finally {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }else{
            //消息确认 出队
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            //消息没有正确处理 出对
            //channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }

    }
}
