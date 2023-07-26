package com.atguigu.gmall.web.controller;


import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.web.controller
 * class:PayController
 *
 * @author: smile
 * @create: 2023/7/24-16:23
 * @Version: v1.0
 * @Description:
 */
@Controller
public class PaymentController {
    @Resource
    private OrderFeignClient orderFeignClient;

    @GetMapping("pay.html")
    public String pay(Model model, HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderFeignClient.getOrderInfoByUserIdAndOrderId(Long.parseLong(orderId));
        /*
        另一种写法
        String orderInfoString = HttpClientUtil
                .doGet("http://localhost:8204/api/order/inner/getOrderInfoByUserIdAndOrderId?orderId=" + orderId + "&userId=" + userId);
        OrderInfo orderInfo = JSONObject.parseObject(orderInfoString, OrderInfo.class);*/
        model.addAttribute("orderInfo", orderInfo);
        return "payment/pay";
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:支付成功页面
     */
    @GetMapping("pay/success.html")
    public String success() {
        return "payment/success";
    }
}
