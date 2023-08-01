package com.atguigu.gmall.order.client.imp;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.order.client.imp
 * class:OrderDegradeFeignClient
 *
 * @author: smile
 * @create: 2023/7/22-15:19
 * @Version: v1.0
 * @Description:
 */
@Component
public class OrderDegradeFeignClient implements OrderFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:封装订单详细页显示数据
     */
    @Override
    public Result<Map<String, Object>> authTrade() {
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取订单信息
     */
    @Override
    public OrderInfo getOrderInfoByUserIdAndOrderId(Long orderId) {
        return null;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }

    @Override
    public Long submitOrder(OrderInfo orderInfo) {
        return null;
    }
}
