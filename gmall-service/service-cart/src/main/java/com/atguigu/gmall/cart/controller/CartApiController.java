package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.addCart(skuId, skuNum, userId);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:展示购物车列表
     * 分别查询登录购物车列表和临时购物车列表
     * 如果用户登录合购物车
     */
    @GetMapping("/cartList")
    public Result<List<CartInfo>> cartList(HttpServletRequest request) {
        //获取用户登录Id
        String userId = AuthContextHolder.getUserId(request);
        //获取用户临时Id
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> list = cartService.cartList(userId, userTempId);
        return Result.ok(list);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更改选中状态
     */
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result<?> checkCart(@PathVariable Long skuId,
                               @PathVariable Integer isChecked,
                               HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        //  判断
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法
        cartService.checkCart(userId, isChecked, skuId);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:全部选中或者取消
     */
    @GetMapping("/allCheckCart/{isChecked}")
    public Result<?> allCheckCart(@PathVariable Integer isChecked, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        this.cartService.allCheckCart(isChecked, userId);
        return Result.ok();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:删除购物车
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result<?> deleteCart(@PathVariable("skuId") Long skuId,
                                HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId, userId);
        return Result.ok();
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:清空购物车
     */
    @GetMapping("clearCart")
    public Result<?> clearCart(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        this.cartService.clearCart(userId);
        return Result.ok();
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取选中购物车列表
     */
    @GetMapping("/getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable("userId") Long userId){
        return cartService.getCartCheckedList(userId);
    }
}
