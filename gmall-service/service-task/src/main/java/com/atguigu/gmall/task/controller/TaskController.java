package com.atguigu.gmall.task.controller;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Controller;
import javax.annotation.Resource;


/**
 * project:gmall-parent
 * package:com.atguigu.gmall.task.controller
 * class:TaskController
 *
 * @author: smile
 * @create: 2023/8/2-9:46
 * @Version: v1.0
 * @Description:
 */
@Controller
public class TaskController {
    @Resource
    private RabbitService rabbitService;

    @XxlJob("demo")
    public void demo(){
        try {
            Thread.sleep(1000);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_1, "老师说传啥都行");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
