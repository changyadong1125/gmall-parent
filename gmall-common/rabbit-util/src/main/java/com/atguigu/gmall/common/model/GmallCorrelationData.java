package com.atguigu.gmall.common.model;

import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;

/**
 * return:
 * author: smile
 * version: 1.0
 * description:
 */
@Data
public class GmallCorrelationData extends CorrelationData {

    //  默认有一个消息唯一表示的Id   private volatile String id;
    //  消息主体
    private Object message;
    //  交换机
    private String exchange;
    //  路由键
    private String routingKey;
    //  重试次数
    private int retryCount = 0;
    //  消息类型  是否是延迟消息
    private boolean isDelay = false;
    //  延迟时间
    private int delayTime = 10;
}