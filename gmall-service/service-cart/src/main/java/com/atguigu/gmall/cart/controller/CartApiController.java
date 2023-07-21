package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.cart.controller
 * class:CartApiController
 *
 * @author: smile
 * @create: 2023/7/21-15:32
 * @Version: v1.0
 * @Description:
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    @Resource
    private CartService cartService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加购物车
     */
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public Result<?> addToCart(@PathVariable Long skuId, @PathVariable Integer skuNum, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.addCart(skuId,skuNum,userId);
        return Result.ok();
    }
}
