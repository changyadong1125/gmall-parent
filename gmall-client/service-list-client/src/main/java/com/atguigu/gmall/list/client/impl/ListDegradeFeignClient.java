package com.atguigu.gmall.list.client.impl;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.stereotype.Component;

import java.util.Map;

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

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据检索条件进行数据检索
     */
    @Override
    public Result<SearchResponseVo>  search(SearchParam searchParam) {
        return null;
    }
}
