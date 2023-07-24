package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.order.service
 * class:OrderService
 *
 * @author: smile
 * @create: 2023/7/22-15:41
 * @Version: v1.0
 * @Description:
 */
public interface OrderService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存订单
     */
    Long saveOrderInfo(OrderInfo orderInfo);
}
