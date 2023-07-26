package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service.impl
 * class:paymentService
 *
 * @author: smile
 * @create: 2023/7/26-14:18
 * @Version: v1.0
 * @Description:
 */
public interface PaymentService {

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存支付订单信息
     */
    void savePaymentInfo(OrderInfo orderInfo,String paymentType);
}
