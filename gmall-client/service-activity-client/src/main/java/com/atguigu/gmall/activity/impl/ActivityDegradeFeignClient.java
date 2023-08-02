package com.atguigu.gmall.activity.impl;


import com.atguigu.gmall.activity.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.item.client.impl
 * class:ItemDegradeFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-18:27
 * @Version: v1.0
 * @Description:
 */
@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {


    @Override
    public Result<List<SeckillGoods>> findAll() {
        return null;
    }

    @Override
    public Result<SeckillGoods> getSeckillGoods(Long skuId) {
        return null;
    }

    @Override
    public Result<Map<String, Object>> seckillTradeData() {
        return null;
    }

    @Override
    public Long submitOrder(OrderInfo orderInfo) {
        return null;
    }
}
