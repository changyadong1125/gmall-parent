package com.atguigu.gmall.payment.controller;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.payment.controller
 * class:AlipayApiController
 *
 * @author: smile
 * @create: 2023/7/26-15:22
 * @Version: v1.0
 * @Description:
 */
@RestController
@RequestMapping("/api/payment/alipay")
@RefreshScope
public class AlipayApiController {
    @Resource
    private AlipayService alipayService;
    @Resource
    private PaymentService paymentService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Value("${app_id}")
    private String app_Id;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:生成支付二维码
     */
    @GetMapping("/submit/{orderId}")
    public String submitPay(@PathVariable Long orderId) {
        return alipayService.createPay(orderId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:跳转到支付成功页面想办法跳转到web_all
     */
    @SneakyThrows
    @GetMapping("/callback/return")
    public void callbackReturn(HttpServletResponse response) {
        response.sendRedirect(AlipayConfig.return_order_url);
    }
   /*
   另一种写法
   public String callbackReturn(){
        return "redirect:"+ AlipayConfig.return_order_url;
    }*/

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:异步回调通知
     * 支付宝会重试我们需要保正幂等性
     * 必须保证是success否则认为通知失败
     */
    @PostMapping("/callback/notify")
    public String callbackNotify(@RequestParam HashMap<String, String> paramsMap) {
        boolean signVerified = false;
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);//调用SDK验证签名
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        if (signVerified) {
            //验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            //获取商户传递的订单号 校验用户订单号
            String outTradeNo = paramsMap.get("out_trade_no");
            //获取异步回调传递的总金额
            String totalAmount = paramsMap.get("total_amount");
            //获取支付产品的appId
            String appId = paramsMap.get("app_id");
            //获取
            String tradeStatus = paramsMap.get("trade_status");
            //获取notifyId
            String notifyId = paramsMap.get("notify_id");
            //利用传递的商户订单号查询paymentInfo或者orderInfo
            PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(outTradeNo);
            //如果为空之返回失败 校验totalAmount
            if (null == paymentInfoQuery || new BigDecimal("0.01").compareTo(new BigDecimal(totalAmount)) != 0||!app_Id.equals(appId)) {
                return "failure";
            }
            //判断幂等性
            Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(notifyId, notifyId, 24 * 60 + 24, TimeUnit.MINUTES);
            if (Boolean.FALSE.equals(aBoolean)) {
                return "failure";
            }
            try {
                //判断支付交易状态
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    //修改交易记录状态 trade_no(支付宝交易hao) payment_status=PAID callback_content
                    //根据什么条件去修改  根据out_trade_no paymentType  或者paymentInfo.getId();
                    this.paymentService.updatePaymentStatus(paymentInfoQuery.getId(),paramsMap);
                    return "success";
                }
            } catch (Exception e) {
                this.redisTemplate.delete(notifyId);
                e.printStackTrace();
            }
        } else {
            //验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:退款接口
     */
    @GetMapping("refund/{orderId}")
    public Result<?> refund(@PathVariable(value = "orderId")Long orderId) {
        // 调用退款接口
        boolean flag = alipayService.refund(orderId);
        return Result.ok(flag);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:关闭支付宝交易
     */
    @GetMapping("closePay/{orderId}")
    public Boolean closePay(@PathVariable Long orderId){
        return alipayService.closePay(orderId);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查询交易记录
     */
    @GetMapping("checkPayment/{orderId}")
    public Boolean checkPayment(@PathVariable Long orderId){
        // 调用退款接口
        return alipayService.checkPayment(orderId);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查看是否有本地交易记录 为什么这里可以直接当参数直接传递进去！
     */
    @GetMapping("/getPaymentInfo/{outTradeNo}")
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo){
        return this.paymentService.getPaymentInfo(outTradeNo);
    }
}