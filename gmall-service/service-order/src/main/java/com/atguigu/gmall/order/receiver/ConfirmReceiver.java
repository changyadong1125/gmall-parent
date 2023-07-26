package com.atguigu.gmall.order.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@Slf4j
public class ConfirmReceiver {
    @Resource
    private OrderService orderService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:监听订单信息
     */
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void receiver(Long orderId, Channel channel, Message message) {
        if (null != orderId) {
            try {
                //判断当前订单的状态
                OrderInfo orderInfo = orderService.getOrderInfo(orderId);
                if (null!=orderInfo&&"UNPAID".equals(orderInfo.getOrderStatus())&&"UNPAID".equals(orderInfo.getProcessStatus())){
                    //取消订单
                    orderService.execExpiredOrder(orderId);
                }
            } catch (Exception e) {
                //如果发生异常 可以进行下一步处理
                log.info("取消订单{}发生异常，订单信息插入数据库！",orderId);
               e.printStackTrace();
            }
            //进行消息确认 防止消息积压
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }
}
