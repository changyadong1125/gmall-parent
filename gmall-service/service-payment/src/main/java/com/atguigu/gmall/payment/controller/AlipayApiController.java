package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class AlipayApiController {

    @Resource
    private AlipayService alipayService;

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
    public void callbackReturn(HttpServletResponse response){
        response.sendRedirect(AlipayConfig.return_order_url);
    }
   /*
   另一种写法
   public String callbackReturn(){
        return "redirect:"+ AlipayConfig.return_order_url;
    }*/
}