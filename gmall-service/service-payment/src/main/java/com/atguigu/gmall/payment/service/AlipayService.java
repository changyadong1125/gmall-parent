package com.atguigu.gmall.payment.service;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.payment.service
 * class:AlipayService
 *
 * @author: smile
 * @create: 2023/7/26-15:24
 * @Version: v1.0
 * @Description:
 */
public interface AlipayService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:支付二维码
     */
    String createPay(Long orderId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:退款接口
     */
    boolean refund(Long orderId);
}
