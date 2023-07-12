package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.item.service
 * class:ItemService
 *
 * @author: smile
 * @create: 2023/7/11-16:29
 * @Version: v1.0
 * @Description:
 */
public interface ItemService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取商品详情数据
     */
    Map<String, Object> getItem(Long skuId);
}
