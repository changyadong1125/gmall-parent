package com.atguigu.gmall.order.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.order.controller
 * class:OrderApiController
 *
 * @author: smile
 * @create: 2023/7/22-14:37
 * @Version: v1.0
 * @Description:
 */
@RefreshScope
@RestController
@RequestMapping("/api/order")
@SuppressWarnings("all")
public class OrderApiController {
    @Resource
    private CartFeignClient cartFeignClient;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private OrderService orderService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:封装订单详细页显示数据
     */
    @GetMapping("/auth/trade")
    public Result<Map<String, Object>> authTrade(HttpServletRequest request) {
        HashMap<String, Object> map = new HashMap<>();
        String userId = AuthContextHolder.getUserId(request);
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(Long.parseLong(userId));
        //需要经CartInfo转成OrderInfo  原子操作类
        AtomicInteger totalNum = new AtomicInteger();
        List<OrderDetail> detailArrayList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            //总数量
            totalNum.addAndGet(orderDetail.getSkuNum());
            return orderDetail;
        }).collect(Collectors.toList());
        //计算总价格
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        map.put("detailArrayList", detailArrayList);
        map.put("totalNum", totalNum);
        map.put("totalAmount", orderInfo.getTotalAmount());
        //存储流水号
        map.put("tradeNo", orderService.getTradeNo(userId));
        return Result.ok(map);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存订单 判断库存是否充足 判断商品价格是否变动 判断是否回退无刷新重复提交
     */
    @PostMapping("auth/submitOrder")
    public Result<?> submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        //防止后退无刷新重复提交
        if (orderService.checkTradeNo(request, userId)) return Result.fail().message("不能重复体提交订单");

        //进行优化 使用completableFuture
        ArrayList<CompletableFuture<Void>> completableFutureArrayList = new ArrayList<>();
        //存储错误信息的集合
        ArrayList<String> errorList = new ArrayList<>();

        //判断库充是否充足 获取订单明细 判断价格是否变动
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //判断
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            for (OrderDetail orderDetail : orderDetailList) {
                //校验库存
                CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                    Boolean result = this.orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                    if (!result) {
                        errorList.add(orderDetail.getSkuName() + "库存不足");
                    }
                }, threadPoolExecutor);
                //将验证库存的线程放进集合
                completableFutureArrayList.add(stockCompletableFuture);
                //校验价格
                CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                    //校验商品价格
                    //实时价格
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                    BigDecimal orderPrice = orderDetail.getOrderPrice();
                    if (skuPrice.compareTo(orderPrice) != 0) {
                        //信息提示涨价还是降价
                        String msg = skuPrice.compareTo(orderPrice) > 0 ? "涨价" : "降价";
                        //提示变动金额
                        BigDecimal price = skuPrice.subtract(orderPrice).abs();
                        //如果价格变动，需要系统自动更新购物车价格 hset key filed value  hget key filed
                        String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
                        CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, orderDetail.getSkuId().toString());
                        if (null != cartInfo) {
                            cartInfo.setSkuPrice(skuPrice);
                            cartInfo.setUpdateTime(new Date());
                            this.redisTemplate.opsForHash().put(cartKey, orderDetail.getSkuId().toString(), cartInfo);
                        }
                        //信息提示
                        errorList.add(orderDetail.getSkuName() + msg + "￥" + price);
                    }
                }, threadPoolExecutor);
                //添加价格线程
                completableFutureArrayList.add(priceCompletableFuture);
            }
        }

        //多任务组合 集合变成刷组
        CompletableFuture.allOf(completableFutureArrayList.toArray(new CompletableFuture[completableFutureArrayList.size()])).join();
        //判断是否发生异常
        if (errorList.size() > 0) {
            //给用户展示有哪些错误
            return Result.fail().message(StringUtils.join(errorList, ","));
        }
        //保存订单
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:订单列表
     */
    @GetMapping("/auth/{page}/{limit}")
    public Result<?> getOrderPage(@PathVariable Long page,
                                  @PathVariable Long limit,
                                  HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        String orderStatus = request.getParameter("orderStatus");
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);
        IPage<OrderInfo> orderPage = orderService.getOrderPage(orderInfoPage, userId, orderStatus);
        return Result.ok(orderPage);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取OrderInfo
     */
    @RequestMapping("/inner/getOrderInfoByUserIdAndOrderId")
    public OrderInfo getOrderInfoByUserIdAndOrderId(HttpServletRequest request ) {
        Long orderId =Long.parseLong(request.getHeader("orderId")) ;
        Long userId =Long.parseLong(request.getHeader("userId")) ;
        return orderService.getOrderInfoByUserIdAndOrderId(userId, orderId);
    }


 /*   @GetMapping("/inner/getOrderInfoByUserIdAndOrderId")
    public String getOrderInfoByUserIdAndOrderId(@RequestParam Long userId,@RequestParam Long orderId) {
        return JSONObject.toJSONString(orderService.getOrderInfoByUserIdAndOrderId(userId, orderId));
    }*/
}
