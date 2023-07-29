package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

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
                if (null != orderInfo && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())) {
                    //取消订单
                    orderService.execExpiredOrder(orderId);
                }
            } catch (Exception e) {
                //如果发生异常 可以进行下一步处理
                log.info("取消订单{}发生异常，订单信息插入数据库！", orderId);
                e.printStackTrace();
            }
            //进行消息确认 防止消息积压
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER_CLOSED,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER_CLOSED),
            key = {MqConst.ROUTING_ORDER_CLOSED}
    ))
    public void orderPayment(Long orderId, Channel channel, Message message) {
        try {
            if (null != orderId) {
                OrderInfo orderInfo = orderService.getOrderInfo(orderId);
                if (null != orderInfo && !OrderStatus.CLOSED.name().equals(orderInfo.getOrderStatus())) {
                    //更新操作
                    orderService.updateOrderStatus(orderId, ProcessStatus.CLOSED);
                }
            }
        } catch (Exception e) {
            //可以处理其他业务
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void paymentPay(Long orderId, Channel channel, Message message){
        try {
            if (null != orderId) {
                OrderInfo orderInfo = orderService.getOrderInfo(orderId);
                if (null != orderInfo && !OrderStatus.PAID.name().equals(orderInfo.getOrderStatus())) {
                    //更新操作
                    orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                    //  发送消息给库存系统
                    orderService.sendDeductStockMsg(orderId);
                }
            }
        } catch (Exception e) {
            //可以处理其他业务
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:监听减库存消息
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    @SuppressWarnings("all")
    public void wareOrder(String wareJson,Channel channel,Message message){
        try {
            if (!StringUtils.isEmpty(wareJson)){
                Map<String,String> map = JSON.parseObject(wareJson, Map.class);
                String orderId = map.get("orderId");
                String status = map.get("status");
                //  判断当前状态
                if ("DEDUCTED".equals(status)){
                    //  已减库存 更新订单状态.
                    this.orderService.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.WAITING_DELEVER);
                }else {
                    //  已减库存 更新订单状态.就需要从其他地方调用商品. 及时不货！如果不货成功，那就再更新一次订单状态。保证的数据最终一致性！
                    this.orderService.updateOrderStatus(Long.parseLong(orderId),ProcessStatus.STOCK_EXCEPTION);
                    //发货失败 记录日志和消息记录表
                    //远程调用其他仓库看是否能够发货 如果能够发货成功就更新订单状态 保证数据最终一致性  （支付和退款保证强一致性 支付系统和积分系统）
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
