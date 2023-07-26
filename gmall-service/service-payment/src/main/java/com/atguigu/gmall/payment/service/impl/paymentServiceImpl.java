package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service.impl
 * class:paymentServiceImpl
 *
 * @author: smile
 * @create: 2023/7/26-14:19
 * @Version: v1.0
 * @Description:
 */
@Service
public class paymentServiceImpl implements PaymentService {
    @Resource
    private PaymentInfoMapper paymentInfoMapper;
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存支付订单信息
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        LambdaQueryWrapper<PaymentInfo> paymentInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        paymentInfoLambdaQueryWrapper.eq(PaymentInfo::getPaymentType,paymentType);
        paymentInfoLambdaQueryWrapper.eq(PaymentInfo::getOrderId,orderInfo.getId());
        PaymentInfo paymentInfoExe = paymentInfoMapper.selectOne(paymentInfoLambdaQueryWrapper);
        if (null!=paymentInfoExe){
            return;
        }
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setUserId(orderInfo.getUserId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setUpdateTime(new Date());
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfoMapper.insert(paymentInfo);
    }
}
