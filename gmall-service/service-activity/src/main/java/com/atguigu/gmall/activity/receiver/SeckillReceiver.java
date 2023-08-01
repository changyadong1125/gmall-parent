package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SeckillReceiver {

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Resource
    private SeckillGoodsService seckillGoodsService;
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加秒杀商品
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_TASK),
            value = @Queue(MqConst.QUEUE_TASK_1),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importSeckillGoodsToRedis(Message message, Channel channel) {
        LambdaQueryWrapper<SeckillGoods> seckillGoodsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        seckillGoodsLambdaQueryWrapper.eq(SeckillGoods::getStatus, "1").gt(SeckillGoods::getStockCount, 0);
        seckillGoodsLambdaQueryWrapper.apply("DATE(start_time) = " + "'" + DateUtil.formatDate(new Date()) + "'");
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(seckillGoodsLambdaQueryWrapper);
        try {
            if (!CollectionUtils.isEmpty(seckillGoods)){
                for (SeckillGoods seckillGood : seckillGoods) {
                    String seckillGoodsKey = RedisConst.SECKILL_GOODS;
                    //判断缓存中是否有改商品
                    Boolean aBoolean = this.redisTemplate.opsForHash().hasKey(seckillGoodsKey, seckillGood.getSkuId().toString());
                    if (aBoolean){
                        //缓存中有数据不能覆盖
                        continue;
                    }
                    redisTemplate.opsForHash().put(seckillGoodsKey,seckillGood.getSkuId().toString(),seckillGood);
                    //如何防止用户超卖问题  放入list
                    for (int i = 0; i < seckillGood.getStockCount(); i++) {
                        //redis本身是单线程的
                        this.redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX+seckillGood.getSkuId(),seckillGood.getSkuId().toString());
                    }
                    //初始化状态位  skuId:1  publish seckillpush 25:1
                    this.redisTemplate.convertAndSend("seckillpush",seckillGood.getSkuId()+":1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:监听下单消息
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void seckillUser(UserRecode userRecode, Message message , Channel channel){
        try {
            //  判断
            if (userRecode!=null){
                //消费消息的业务处理
                seckillGoodsService.seckillUser(userRecode);
            }
        } catch (Exception e) {
            log.error("处理队列中的数据处理失败:{}",e.getMessage());
            throw new RuntimeException(e);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:监听消息 修改减库存
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_STOCK,durable = "true",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_STOCK),
            key = {MqConst.ROUTING_SECKILL_STOCK}
    ))
    public void seckillStock(Long skuId, Message message ,Channel channel){
        try {
            if (null!=skuId){
                seckillGoodsService.updateStock(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:清空队列
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_TASK),
            value = @Queue(MqConst.QUEUE_TASK_2),
            key = {MqConst.ROUTING_TASK_2}
    ))
    public void clearDate(Message message, Channel channel) {
        LambdaQueryWrapper<SeckillGoods> seckillGoodsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        seckillGoodsLambdaQueryWrapper.eq(SeckillGoods::getStatus, "1");
        seckillGoodsLambdaQueryWrapper.le(SeckillGoods::getEndTime,new Date());
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(seckillGoodsLambdaQueryWrapper);
        try {
            if (!CollectionUtils.isEmpty(seckillGoods)){
                //删除库存
                for (SeckillGoods seckillGood : seckillGoods) {
                    this.redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX+seckillGood.getSkuId());
                }
            }
            //删除商品集合
            this.redisTemplate.delete(RedisConst.SECKILL_GOODS);
            //删除预下单数据
            this.redisTemplate.delete(RedisConst.SECKILL_ORDERS);
            //删除真正下单数据
            this.redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);    }
}