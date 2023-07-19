package com.atguigu.gmall.list.client;

import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

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
@FeignClient(value = "service-list", path = "/api/list", fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新商品的热度排名分值
     */
    @GetMapping("/inner/incrHotScore/{skuId}")
    Result<?> incrHotScore(@PathVariable("skuId") Long skuId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据检索条件进行数据检索
     */
    @PostMapping
    Result<SearchResponseVo>  search(@RequestBody SearchParam searchParam);
}
