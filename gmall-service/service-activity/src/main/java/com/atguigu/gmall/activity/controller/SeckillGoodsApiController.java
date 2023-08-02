package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HttpServletBean;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsApiController {

    @Resource
    private SeckillGoodsService seckillGoodsService;
    @Resource
    private OrderFeignClient orderFeignClient;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取列表
     */
    @GetMapping("/findAll")
    public Result<List<SeckillGoods>> findAll() {
        List<SeckillGoods> list = seckillGoodsService.findAll();
        return Result.ok(list);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取实体
     */
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result<SeckillGoods> getSeckillGoods(@PathVariable("skuId") Long skuId) {
        return Result.ok(seckillGoodsService.getSeckillGoods(skuId));
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result<?> getSeckillSkuIdStr(@PathVariable Long skuId, HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        SeckillGoods seckillGoods = this.seckillGoodsService.getSeckillGoods(skuId);
        if (null != seckillGoods) {
            //获取当前系统时间
            Date currentTime = new Date();
            if (DateUtil.dateCompare(seckillGoods.getStartTime(), currentTime) && DateUtil.dateCompare(currentTime, seckillGoods.getEndTime())) {
                //生成抢购吗
                String encrypt = MD5.encrypt(userId);
                return Result.ok(encrypt);
            }
        }
        return Result.fail().message("生成抢购吗失败！");
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:预下单
     */
    @PostMapping("/auth/seckillOrder/{skuId}")
    public Result<?> seckillOrder(@PathVariable Long skuId, HttpServletRequest request) {
        //  获取到抢购码
        String skuIdStr = request.getParameter("skuIdStr");
        //  获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        //  返回result
        return seckillGoodsService.seckillOrder(skuId, skuIdStr, userId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:轮训查单
     */
    @GetMapping("/auth/checkOrder/{skuId}")
    public Result<?> checkOrder(@PathVariable Long skuId, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        return seckillGoodsService.checkOrder(userId, skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:秒杀订单确认页面渲染需要参数汇总接口
     */
    @GetMapping("/auth/trade")
    public Result<Map<String, Object>> seckillTradeData(HttpServletRequest request) {
        //获取到秒杀商品 通过userId 赋值给detailArrayList
        String userId = AuthContextHolder.getUserId(request);
        return Result.ok(seckillGoodsService.seckillTradeData(userId));
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:秒杀模块下单
     */
    @PostMapping("/auth/submitOrder")
    public Result<?> submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        //保存之前需要给订单对象 赋值userId
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        //调用服务层方法，在订单微服务中已经有一个保存订单的实现类 所以我们直接调用
        Long orderId = this.orderFeignClient.submitOrder(orderInfo);
        if (null == orderId) {
            return Result.fail().message("保存订单失败！");
        }
        //删除预下单数据 做真正的订单保存
        //  删除预下单数据： Hdel key field
        this.redisTemplate.opsForHash().delete(RedisConst.SECKILL_ORDERS, userId);
        //  重新保存真正的订单数据到缓存.
        //  key = seckill:orders:users field = userId  value = orderId  hget key field;
        this.redisTemplate.opsForHash().put(RedisConst.SECKILL_ORDERS_USERS, userId, orderId.toString());
        //  返回订单Id
        return Result.ok(orderId);
    }
}