package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

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
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:展示购物车列表
     * 分别查询登录购物车列表和临时购物车列表
     * 如果用户登录合购物车
     */
    List<CartInfo> cartList(String userId, String userTempId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更改选中状态
     */
    void checkCart(String userId, Integer isChecked, Long skuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:全部选中或者取消
     */
    void allCheckCart(Integer isChecked, String userId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:删除购物车
     */
    void deleteCart(Long skuId, String userId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:清空购物车
     */
    void clearCart(String userId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取选中购物车列表
     */
    List<CartInfo> getCartCheckedList(Long userId);
}
