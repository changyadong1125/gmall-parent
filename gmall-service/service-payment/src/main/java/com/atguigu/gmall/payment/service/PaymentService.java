package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.HashMap;

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

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据交易订单号获取交易订单信息
     */
    PaymentInfo getPaymentInfo(String outTradeNo);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改订单状态
     */
    void updatePaymentStatus(Long id, HashMap<String, String> paramsMap);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:方法重载
     */
    void updatePaymentStatus(String outTradeNo, PaymentInfo paymentInfo);
}
