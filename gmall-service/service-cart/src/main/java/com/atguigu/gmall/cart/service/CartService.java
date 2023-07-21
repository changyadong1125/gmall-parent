package com.atguigu.gmall.cart.service;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.cart.service
 * class:CartService
 *
 * @author: smile
 * @create: 2023/7/21-15:48
 * @Version: v1.0
 * @Description:
 */
public interface CartService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加购物车
     */
    void addCart(Long skuId, Integer skuNum, String userId);
}
