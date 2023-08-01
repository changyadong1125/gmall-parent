package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.model.order.OrderDetail;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.activity.service.impl
 * class:SeckillGoodsServiceImp
 *
 * @author: smile
 * @create: 2023/8/1-10:13
 * @Version: v1.0
 * @Description:
 */
@Service
public class SeckillGoodsServiceImp implements SeckillGoodsService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查询所有
     */
    @Override
    public List<SeckillGoods> findAll() {
        return this.redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS).stream().map(A -> (SeckillGoods) A).collect(Collectors.toList());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取实体
     */
    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        return (SeckillGoods) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS, skuId.toString());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:预下单
     */
    @Override
    public Result<?> seckillOrder(Long skuId, String skuIdStr, String userId) {
        if (!skuIdStr.equals(MD5.encrypt(userId))) {
            return Result.fail().message("校验抢购码失败，属于非法抢购！");
        }
        String status = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(status) || "0".equals(status)) {
            return Result.fail().message("当前商品不可抢购，售空！");
        }
        //可以抢购 将用户信息放入对列中一个一个处理
        UserRecode userRecode = new UserRecode();
        userRecode.setSkuId(skuId);
        userRecode.setUserId(userId);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER, MqConst.ROUTING_SECKILL_USER, userRecode);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:处理预下单
     */
    @Override
    public void seckillUser(UserRecode userRecode) {
        //判断状态位
        String status = (String) CacheHelper.get(userRecode.getSkuId().toString());
        if (StringUtils.isEmpty(status) || "0".equals(status)) {
            return;
        }
        //判断用户是否秒杀过 setnx
        String seckillUserKey = RedisConst.SECKILL_USER + userRecode.getUserId() + userRecode.getSkuId();
        //秒杀内不能重复购买
        Boolean aBoolean = this.redisTemplate.opsForValue().setIfAbsent(seckillUserKey, userRecode.getUserId(), RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(aBoolean)) {
            return;
        }
        //从list中获取元素
        String skuIdExist = this.redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX + userRecode.getSkuId());
        if (StringUtils.isEmpty(skuIdExist)) {
            //售空
            this.redisTemplate.convertAndSend("seckillpush", userRecode.getSkuId() + "0");
            return;
        }
        //自定义实体类存储秒杀信息数据  orderRecode
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userRecode.getUserId());
        //再秒杀的时候页面没有购买数量入口  限制一件
        //  防止超卖每次给1件
        orderRecode.setNum(1);
        orderRecode.setSeckillGoods(this.getSeckillGoods(userRecode.getSkuId()));
        //  下单码
        orderRecode.setOrderStr(MD5.encrypt(userRecode.getUserId() + userRecode.getSkuId()));
        //存储到缓存
        this.redisTemplate.opsForHash().put(RedisConst.SECKILL_ORDERS, userRecode.getUserId(), orderRecode);
        //  修改剩余库存数量. 异步：发送消息！ redis + mysql
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_STOCK, MqConst.ROUTING_SECKILL_STOCK, userRecode.getSkuId());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新剩余库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long skuId) {
        //现在还有多少库存  查看对列的长度
        Long count = this.redisTemplate.opsForList().size(RedisConst.SECKILL_STOCK_PREFIX + skuId);
        //获取缓存中的数据
        SeckillGoods seckillGoods = (SeckillGoods) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS, skuId.toString());
        //更新缓存
        assert seckillGoods != null;
        assert count != null;
        seckillGoods.setStockCount(count.intValue());
        //更新数据库 先更新数据库 可以通过事务保整成功执行 之后在执行redis
        seckillGoodsMapper.updateById(seckillGoods);
        //写回缓存
        this.redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS, skuId.toString(), seckillGoods);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:轮训查单查看订单状态
     */
    @Override
    public Result<?> checkOrder(String userId, Long skuId) {
        //判断用户在缓存中是否存在
        String seckillUserKey = RedisConst.SECKILL_USER + userId + skuId;
        Boolean userExit = this.redisTemplate.hasKey(seckillUserKey);
        if (Boolean.TRUE.equals(userExit)){
            //说明用户存在 只有在存在的时候才有可能秒杀成功 获取订单信息
            OrderRecode orderRecode = (OrderRecode) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS, userId);
            if (null!=orderRecode){
                //说明秒杀成功
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }
        //判断这个用户是否有过真的下单记录 到提交订单的时候的的时候才交叫真正的下单
        //判断缓存中是会否有真正的下单记录
        //key = seckill:order:users  field = userId  value = orderId
        String  orderId = (String) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS_USERS, userId);
        //判断
        if (null!=orderId){
            //说明秒杀成功 已经有过记录
            return Result.build(orderId,ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        //判断一个状态位
        String status = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(status)||"0".equals(status)){
            //判断上品状态位
            return Result.build(null,ResultCodeEnum.SECKILL_FAIL);
        }
        //默认排队
        return Result.build(null,ResultCodeEnum.SECKILL_RUN);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:秒杀订单确认页面渲染需要参数汇总接口
     */
    @Override
    public Map<String,Object> seckillTradeData(String userId) {
        HashMap<String, Object> map = new HashMap<>();
        OrderRecode orderRecode = (OrderRecode) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS, userId);
        //  判断
        if (orderRecode == null){
            throw new RuntimeException("缓存中不存在预下单数据.");
        }
        //  获取秒杀商品对象
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        //  需要将秒杀商品变为 OrderDetail;
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        // 声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        detailArrayList.add(orderDetail);
        //给map赋值
        map.put("detailArrayList",detailArrayList);
        map.put("totalAmount",seckillGoods.getCostPrice());
        map.put("totalNum",orderRecode.getNum());
        return map;
    }
}
