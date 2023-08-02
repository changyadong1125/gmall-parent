package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import lombok.SneakyThrows;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.payment.service.impl
 * class:AlipayServiceImpl
 *
 * @author: smile
 * @create: 2023/7/26-15:24
 * @Version: v1.0
 * @Description:
 */
@Service
@RefreshScope
public class AlipayServiceImpl implements AlipayService {
    @Resource
    private OrderFeignClient orderFeignClient;
    @Resource
    private PaymentService paymentService;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private RabbitService rabbitService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:支付订单二维码
     */
    @Override
    public String createPay(Long orderId) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        paymentService.savePaymentInfo(orderInfo, PaymentType.ALIPAY.name());
        if ("PAID".equals(orderInfo.getOrderStatus()) || "CLOSED".equals(orderInfo.getOrderStatus())) {
            return "当前订单已支付或关闭";
        }
        /*
        从ioc获取
        AlipayClient alipayClient = new DefaultAlipayClient(
               "https://openapi.alipay.com/gateway.do",
               "app_id",
               "your private_key",
               "json",
               "GBK",
               "alipay_public_key",
               "RSA2");
        */
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //异步接收地址，仅支持http/https，公网可访问  内网穿透工具没开一次换一次 需要修改配置文件
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        //同步跳转地址，仅支持http/https
        request.setReturnUrl(AlipayConfig.return_payment_url);
        //必传参数
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", 0.01);
        //订单标题，不可使用特殊符号
        bizContent.put("subject", "花钱买个教训");
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        //可选参数
        //获取当前时间 在当前时间基础上加十分钟
        //过期时间和有效期作比较 二维码的有效期和订单的有效期做比较
        Instant instant = LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant();
        java.util.Date date = Date.from(instant);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date expireTime = orderInfo.getExpireTime();
        String dateString = format.format(DateUtil.dateCompare(expireTime, date) ? expireTime : date);
        //如果expireTime订单过期时间比date晚 返回false
        bizContent.put("time_expire", dateString);

        /*
         商品明细信息，按需传入
        JSONArray goodsDetail = new JSONArray();
        JSONObject goods1 = new JSONObject();
        goods1.put("goods_id", "goodsNo1");
        goods1.put("goods_name", "子商品1");
        goods1.put("quantity", 1);
        goods1.put("price", 0.01);
        goodsDetail.add(goods1);
        bizContent.put("goods_detail", goodsDetail);
        // 扩展信息，按需传入
        JSONObject extendParams = new JSONObject();
        extendParams.put("sys_service_provider_id", "2088511833207846");
        bizContent.put("extend_params", extendParams);
        */

        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        return response.getBody();
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:退款接口
     */
    @SneakyThrows
    @Override
    public boolean refund(Long orderId) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        //查询out_trade_no
        OrderInfo orderInfo = this.orderFeignClient.getOrderInfo(orderId);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        bizContent.put("refund_amount", 0.01);

        /*
        返回参数选项，按需传入
        bizContent.put("out_request_no", "HZ01RF001");
        JSONArray queryOptions = new JSONArray();
        queryOptions.add("refund_detail_item_list");
        bizContent.put("query_options", queryOptions);*/

        request.setBizContent(bizContent.toString());
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            if ("Y".equals(response.getFundChange())) {
                System.out.println("调用成功");
                //修改交易记录状态 payment_status
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
                this.paymentService.updatePaymentStatus(orderInfo.getOutTradeNo(), paymentInfo);
                //修改订单状态
                this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER_CLOSED, MqConst.ROUTING_ORDER_CLOSED, orderId);
                return true;
            } else {
                return false;
            }
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:关闭支付宝交易
     */
    @SneakyThrows
    @Override
    public Boolean closePay(Long orderId) {
        //获取订单对象
        OrderInfo orderInfo = this.orderFeignClient.getOrderInfo(orderId);
        if (orderInfo == null) {
            return false;
        }
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        //只有支付成功之后才会有值
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        request.setBizContent(bizContent.toString());
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功");
            //如果支付宝关闭成功 就必须要关闭交易记录和订单记录
            //什么时候需要关闭订单
            // 一种是已经支付的交易记录不能关闭
            // 一种是没有支付的交易记录未扫码（表示交易不存在）
            // 扫码未支付的订单可以取消
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查询交易记录
     */
    @SneakyThrows
    @Override
    public Boolean checkPayment(Long orderId) {
        OrderInfo orderInfo = this.orderFeignClient.getOrderInfo(orderId);
        if (orderInfo == null) {
            return false;
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
           /*可以更加细致的判断
            String tradeStatus = response.getTradeStatus();
            if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                log.info("等待付款");
            } else if ("TRADE_SUCCESS".equals(tradeStatus)) {
                log.info("支付成功");
            } else {
                log.info("扫码未支付超时关闭");
            }*/
            //进行支付过的，提示tradeSuccess
            //已经关闭的，提示tradeClosed
            //扫码未支付，提示wait_buyer_pay
            //未扫码，提示交易不存在
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }
    }
}
