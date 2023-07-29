package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;

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
@RefreshScope
public class paymentServiceImpl implements PaymentService {
    @Resource
    private PaymentInfoMapper paymentInfoMapper;
    @Resource
    private RabbitService rabbitService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存支付订单信息
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        LambdaQueryWrapper<PaymentInfo> paymentInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        paymentInfoLambdaQueryWrapper.eq(PaymentInfo::getPaymentType, paymentType);
        paymentInfoLambdaQueryWrapper.eq(PaymentInfo::getOrderId, orderInfo.getId());
        PaymentInfo paymentInfoExe = paymentInfoMapper.selectOne(paymentInfoLambdaQueryWrapper);
        if (null != paymentInfoExe) {
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

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据交易订单号获取交易订单信息
     */
    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo) {
        return paymentInfoMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOutTradeNo, outTradeNo));
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改订单状态
     */
    @Override
    public void updatePaymentStatus(Long id, HashMap<String, String> paramsMap) {
        //paymentInfo中获取orderId
        PaymentInfo paymentInfo = this.getPaymentInfo(paramsMap.get("out_trade_no"));
        paymentInfo.setId(id);
        paymentInfo.setTradeNo(paramsMap.get("trade_no"));
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(paramsMap.toString());
        //修改交易内容
        paymentInfoMapper.updateById(paymentInfo);
        //发送一个消息给订单 减库存
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,paymentInfo.getOrderId());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:方法重载
     */
    @Override
    public void updatePaymentStatus(String outTradeNo, PaymentInfo paymentInfo) {
        LambdaQueryWrapper<PaymentInfo> paymentInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        paymentInfoLambdaQueryWrapper.eq(PaymentInfo::getOutTradeNo, outTradeNo);
        paymentInfoMapper.update(paymentInfo, paymentInfoLambdaQueryWrapper);
    }
}
