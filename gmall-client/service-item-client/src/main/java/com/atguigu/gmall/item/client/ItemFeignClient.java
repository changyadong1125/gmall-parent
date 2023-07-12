package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
@FeignClient(value = "service-item",path = "/api/item",fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取商品详情数据
     */
    @GetMapping("/{skuId}")
    Result<Map<String,Object>> getItem(@PathVariable Long skuId);
}
