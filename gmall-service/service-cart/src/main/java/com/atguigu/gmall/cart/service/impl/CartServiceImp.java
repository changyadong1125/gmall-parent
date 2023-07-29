package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    private RedisTemplate<String, CartInfo> redisTemplate;
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
            //设置每个商品的最大购买数量
            Integer num = Math.min(cartInfoExist.getSkuNum() + skuNum, 200);
            cartInfoExist.setSkuNum(num);
            cartInfoExist.setSkuPrice(this.productFeignClient.getSkuPrice(skuId));
            //从新修改赋值时间
            cartInfoExist.setUpdateTime(new Date());
            //每次添加都是选中状态
            if (cartInfoExist.getIsChecked() == 0) {
                cartInfoExist.setIsChecked(1);
            }
            //保存
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

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:展示购物车列表
     * 分别查询登录购物车列表和临时购物车列表
     * 如果用户登录合购物车
     */
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {
        //购物车集合列表
        List<CartInfo> loginCartInfoList = null;
        //如果userId和userTempId都存在 并且未登录购物车集合有数据的情况下才能合并
        //未登录购物车集合列表
        List<CartInfo> tempCartInfoList = null;
        //case1：userTempId=111  case2：userTempId=111 userId=1
        //判断临时用户Id不为空
        if (!StringUtils.isEmpty(userTempId)) {
            String cartKey = this.getCartKey(userTempId);
            tempCartInfoList = this.redisTemplate.opsForHash().values(cartKey).stream().map(A -> (CartInfo) A).collect(Collectors.toList());
            //判断临时购物车是否为空
            if (!CollectionUtils.isEmpty(tempCartInfoList)) {
//                tempCartInfoList.sort(Comparator.comparing(BaseEntity::getUpdateTime));
                tempCartInfoList.sort((A, B) -> DateUtil.truncatedCompareTo(B.getUpdateTime(), A.getUpdateTime(), Calendar.SECOND));
            }
            //判断用户Id为空的时候，直接返回未登录的购物车集合  如果用户id为空临时购物车也为空也直接返回
            if (StringUtils.isEmpty(userId)) {
                //返回未登录购物车集合
                return tempCartInfoList;
            }
        }
        //判断用户Id不为空 且购物车不为空 进行合并
        if (!StringUtils.isEmpty(userId)) {
            //查看登录购物车
            //获取登录购物车数据
            String cartKey = this.getCartKey(userId);
            //获取购物车对象
            BoundHashOperations<String, String, CartInfo> boundHashOperations = this.redisTemplate.boundHashOps(cartKey);
            //hget key filed = boundHashOperations.get(skuId.toString());
            //判断临时购物车是否有数据
            if (!CollectionUtils.isEmpty(tempCartInfoList)) {
                //如果有数据进行合并 否则排序后直接返回用户购物车
                tempCartInfoList.forEach(tempCartInfo -> {
                    //判断skuId是否相等
                    if (Boolean.TRUE.equals(boundHashOperations.hasKey(tempCartInfo.getSkuId().toString()))) {
                        //临时购物车和用户购物车都有的数据
                        //设置商品数量
                        CartInfo cartInfo = boundHashOperations.get(tempCartInfo.getSkuId());
                        assert cartInfo != null;
                        cartInfo.setSkuNum(cartInfo.getSkuNum() + tempCartInfo.getSkuNum());
                        //设置更新时间
                        cartInfo.setUpdateTime(new Date());
                        //判断是否选中
                        if (tempCartInfo.getIsChecked() == 1) {
                            cartInfo.setIsChecked(1);
                        }
                        //更新购物车中已有的数据
                        boundHashOperations.put(tempCartInfo.getSkuId().toString(), cartInfo);
                    } else {
                        //添加临时购物车自己有的数据
                        tempCartInfo.setUserId(userId);
                        tempCartInfo.setCreateTime(new Date());
                        tempCartInfo.setUpdateTime(new Date());
                        boundHashOperations.put(tempCartInfo.getSkuId().toString(), tempCartInfo);
                    }
                });
                //删除未登录的购物车数据
                this.redisTemplate.delete(this.getCartKey(userTempId));
            }
            //合并之后的购物车数据
            loginCartInfoList = boundHashOperations.values();
            //如果为空返回空
            if (CollectionUtils.isEmpty(loginCartInfoList)) {
                return new ArrayList<>();
            }
            //如果不为空进行排序后返回
            loginCartInfoList.sort((A, B) -> DateUtil.truncatedCompareTo(B.getUpdateTime(), A.getUpdateTime(), Calendar.SECOND));
        }
        //最后返回用户购物车数据
        return loginCartInfoList;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更改选中状态
     */
    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(this.getCartKey(userId), skuId.toString());
        if (null != cartInfo) {
            cartInfo.setIsChecked(isChecked);
            this.redisTemplate.opsForHash().put(this.getCartKey(userId), skuId.toString(), cartInfo);
        }
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:全部选中或者取消
     */
    public void allCheckCart(Integer isChecked, String userId) {
        String cartKey = this.getCartKey(userId);
        List<CartInfo> cartInfoList = Objects.requireNonNull(this.redisTemplate.boundHashOps(cartKey).values()).stream().map(A -> (CartInfo) A).collect(Collectors.toList());
        Map<String, CartInfo> map = cartInfoList.stream().peek(cartInfo -> cartInfo.setIsChecked(isChecked)).collect(Collectors.toMap(cartInfo -> cartInfo.getSkuId().toString(), cartInfo -> cartInfo));
        this.redisTemplate.opsForHash().putAll(cartKey, map);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:删除购物车
     */
    @Override
    public void deleteCart(Long skuId, String userId) {
        this.redisTemplate.boundHashOps(this.getCartKey(userId)).delete(skuId.toString());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:清空购物车
     */
    @Override
    public void clearCart(String userId) {
        this.redisTemplate.delete(this.getCartKey(userId));
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取选中购物车列表
     */
    @Override
    public List<CartInfo> getCartCheckedList(Long userId) {
        String cartKey = this.getCartKey(userId.toString());
        List<CartInfo> cartInfoList = Objects.requireNonNull(this.redisTemplate.boundHashOps(cartKey).values()).stream().map(A -> (CartInfo) A).collect(Collectors.toList());
        return cartInfoList.stream().filter(cartInfo -> cartInfo.getIsChecked() == 1).collect(Collectors.toList());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取购物车缓存key
     */
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
