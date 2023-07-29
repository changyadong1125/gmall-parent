package com.atguigu.gmall.task.scheduled;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@EnableScheduling
public class ScheduledTask {

    @Resource
    private RabbitService rabbitService;

    /**
     * 正式每天凌晨1点执行
     * 测试每隔30s执行
     */
    //@Scheduled(cron = "0/30 * * * * ?")
    @Scheduled(cron = "0/5 * * * * ?")
    public void task1() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_1, "老师说传啥都行");
    }
}