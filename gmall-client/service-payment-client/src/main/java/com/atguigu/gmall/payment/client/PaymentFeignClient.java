package com.atguigu.gmall.payment.client;

import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.client.impl.PaymentDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-payment", path = "/api/payment/alipay", fallback = PaymentDegradeFeignClient.class)
public interface PaymentFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:关闭支付宝交易
     */
    @GetMapping("closePay/{orderId}")
    public Boolean closePay(@PathVariable Long orderId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查询交易记录
     */
    @GetMapping("checkPayment/{orderId}")
    public Boolean checkPayment(@PathVariable Long orderId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查看是否有本地交易记录 为什么这里可以直接当参数直接传递进去！
     */
    @GetMapping("/getPaymentInfo/{outTradeNo}")
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo);
}