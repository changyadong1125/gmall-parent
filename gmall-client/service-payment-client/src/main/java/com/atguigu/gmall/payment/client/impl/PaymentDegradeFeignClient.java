package com.atguigu.gmall.payment.client.impl;

import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import org.springframework.stereotype.Component;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.payment.client.impl
 * class:PaymentDegradeFeignClient
 *
 * @author: smile
 * @create: 2023/7/29-11:16
 * @Version: v1.0
 * @Description:
 */
@Component
public class PaymentDegradeFeignClient implements PaymentFeignClient {
    @Override
    public Boolean closePay(Long orderId) {
        return null;
    }

    @Override
    public Boolean checkPayment(Long orderId) {
        return null;
    }

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {
        return null;
    }
}
