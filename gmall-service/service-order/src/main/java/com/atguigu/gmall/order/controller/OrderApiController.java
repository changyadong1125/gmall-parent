package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class OrderApiController {
    @Resource
    private CartFeignClient cartFeignClient;
    @Resource
    private OrderService orderService;

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
        return Result.ok(map);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存订单
     */
    @PostMapping("auth/submitOrder")
    public Result<?> submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {

        // 获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        // 验证通过，保存订单！
        Long orderId = orderService.saveOrderInfo(orderInfo);

        return Result.ok(orderId);
    }

}
