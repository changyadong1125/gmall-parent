package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
public class ItemDegradeFeignClient implements ItemFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取商品详情数据
     */
    public Result<Map<String,Object>> getItem(@PathVariable Long skuId) {
        return null;
    }
}
