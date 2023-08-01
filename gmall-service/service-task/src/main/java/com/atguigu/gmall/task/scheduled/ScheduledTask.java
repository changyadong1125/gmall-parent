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
     * return:
     * author: smile
     * version: 1.0
     * description:
     *  1.基本格式[用空格分开的七部分时间元素]
     * 		按顺序依次为
     * 			①秒（0~59）
     * 			②分钟（0~59）
     * 			③小时（0~23）
     * 			④天（月）（0~30，但是你需要考虑你月的天数）
     * 			⑤月【day of month】（0~11）
     * 			⑥天（星期）【day of week】（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT）
     * 			⑦年份（1970－2099）
     * 		设置方式：
     * 		     ①、指定具体值:5
     * 			 ②、连续区间:9-12
     * 			 ③、有间隔的区间:8-18/4
     * 			     斜杠后面为间隔跨度
     * 			 ④、具体值的列表 4,5,7,8,10
     */

    @Scheduled(cron = "0/10 * * * * ?")
    public void task1() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_1, "老师说传啥都行");
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:每天十八点以后清空缓存
     */
    @Scheduled(cron = "0 0 0/3 * * ?")
    public void task2() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_2, "清空数据~~~~");
    }
}