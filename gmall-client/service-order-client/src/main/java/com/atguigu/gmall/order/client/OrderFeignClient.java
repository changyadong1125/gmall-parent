package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.imp.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.order.client
 * class:OrderFeignClient
 *
 * @author: smile
 * @create: 2023/7/22-15:18
 * @Version: v1.0
 * @Description:
 */
@FeignClient(value = "service-order", path = "/api/order", fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:封装订单详细页显示数据
     */
    @GetMapping("/auth/trade")
    Result<Map<String, Object>> authTrade();

    @RequestMapping("/inner/getOrderInfoByUserIdAndOrderId")
//    OrderInfo getOrderInfoByUserIdAndOrderId(HashMap<String, Long> map ) ;
    OrderInfo getOrderInfoByUserIdAndOrderId();
}
