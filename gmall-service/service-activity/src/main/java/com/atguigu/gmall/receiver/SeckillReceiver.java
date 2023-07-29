package com.atguigu.gmall.receiver;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RedisTemplate<String,String> redisTemplate;

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
                    redisTemplate.opsForHash().put(seckillGoodsKey,seckillGood.getSkuId(),seckillGood);
                    //如何防止用户超卖问题  放入list
                    for (Integer i = 0; i < seckillGood.getStockCount(); i++) {
                        //redis本身是单线程的
                        this.redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX+seckillGood.getSkuId(),seckillGood.getSkuId().toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}