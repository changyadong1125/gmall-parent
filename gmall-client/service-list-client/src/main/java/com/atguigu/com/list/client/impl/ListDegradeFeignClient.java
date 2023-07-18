package com.atguigu.com.list.client.impl;

import com.atguigu.com.list.client.ListFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

/**
 * project:gmall-parent
 * package:com.atguigu.com.list.client.impl
 * class:ListDegradeFeignClient
 *
 * @author: smile
 * @create: 2023/7/17-19:41
 * @Version: v1.0
 * @Description:
 */
@Component
public class ListDegradeFeignClient implements ListFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新商品的热度排名分值
     */
    @Override
    public Result<?> incrHotScore(Long skuId) {
        return null;
    }
}
