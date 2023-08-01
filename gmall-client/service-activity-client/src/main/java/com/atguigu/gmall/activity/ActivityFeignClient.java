package com.atguigu.gmall.activity;
import com.atguigu.gmall.activity.impl.ActivityDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.item.client.impl
 * class:ItemFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-18:27
 * @Version: v1.0
 * @Description:
 */
@FeignClient(value = "service-activity", fallback = ActivityDegradeFeignClient.class)
public interface ActivityFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取列表
     */
    @GetMapping("/api/activity/seckill/findAll")
     Result<List<SeckillGoods>> findAll();

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取实体
     */
    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
     Result<SeckillGoods> getSeckillGoods(@PathVariable("skuId") Long skuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:秒杀订单确认页面渲染需要参数汇总接口
     */
    @GetMapping("/api/activity/seckill/auth/trade")
     Result<Map<String, Object>> seckillTradeData();
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:提交订单
     */
    @PostMapping("/api/order/inner/seckill/submitOrder")
    Long submitOrder(@RequestBody OrderInfo orderInfo);
}
