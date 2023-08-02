package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;

import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.activity.service
 * class:SeckillGoodsService
 *
 * @author: smile
 * @create: 2023/8/1-10:13
 * @Version: v1.0
 * @Description:
 */
public interface SeckillGoodsService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查询所有
     */
    List<SeckillGoods> findAll();

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取实体
     */
    SeckillGoods getSeckillGoods(Long skuId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:预下单
     */
    Result<?> seckillOrder(Long skuId, String skuIdStr, String userId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:处理预下单订单
     */
    void seckillUser(UserRecode userRecode);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新剩余库存
     */
    void updateStock(Long skuId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:轮训查单 查看订单状态
     */
    Result<?> checkOrder(String userId, Long skuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:秒杀订单确认页面渲染需要参数汇总接口
     */
    Map<String,Object> seckillTradeData(String userId);
}
