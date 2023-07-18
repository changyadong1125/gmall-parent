package com.atguigu.com.list.client;

import com.atguigu.com.list.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * project:gmall-parent
 * package:com.atguigu.com.list.client.impl
 * class:ListFeignClient
 *
 * @author: smile
 * @create: 2023/7/17-19:37
 * @Version: v1.0
 * @Description:
 */
@FeignClient(value = "service-list",path = "/api/list",fallback = ListDegradeFeignClient.class )
public interface ListFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新商品的热度排名分值
     */
    @GetMapping("/inner/incrHotScore/{skuId}")
     Result<?> incrHotScore(@PathVariable("skuId") Long skuId);

}
