package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.cart.service.impl
 * class:CartServiceImpl
 *
 * @author: smile
 * @create: 2023/7/21-15:49
 * @Version: v1.0
 * @Description:
 */
@Service
public class CartServiceImp implements CartService {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private ProductFeignClient productFeignClient;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加购物车
     */
    @Override
    public void addCart(Long skuId, Integer skuNum, String userId) {
        //判断购物车是否有当前商品
        //获取购物车的key
        String cartKey = this.getCartKey(userId);
        //hget key field
        CartInfo cartInfoExist = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (null != cartInfoExist) {
            //  说明有这个商品; 每个商品最多购买200件
            Integer num = Math.min(cartInfoExist.getSkuNum() + skuNum, 200);
            cartInfoExist.setSkuNum(num);
            cartInfoExist.setSkuPrice(this.productFeignClient.getSkuPrice(skuId));
            //从新修改赋值时间
            cartInfoExist.setUpdateTime(new Date());
            //每次添加都是选中状态
            if (cartInfoExist.getIsChecked() == 0) {
                cartInfoExist.setIsChecked(1);
            }
            //写到缓存
        } else {
            SkuInfo skuInfo = this.productFeignClient.getSkuInfo(skuId);
            cartInfoExist = new CartInfo();
            cartInfoExist.setUserId(userId);
            cartInfoExist.setSkuId(skuId);
            cartInfoExist.setSkuNum(skuNum);
            cartInfoExist.setSkuName(skuInfo.getSkuName());
            cartInfoExist.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoExist.setCartPrice(skuInfo.getPrice());
            cartInfoExist.setSkuPrice(this.productFeignClient.getSkuPrice(skuId));
            cartInfoExist.setCreateTime(new Date());
            cartInfoExist.setUpdateTime(new Date());
        }
        this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfoExist);
    }

    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX+userId+RedisConst.USER_CART_KEY_SUFFIX;
    }
}
