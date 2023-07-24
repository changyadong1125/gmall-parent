package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.web.controller
 * class:OrderController
 *
 * @author: smile
 * @create: 2023/7/22-14:40
 * @Version: v1.0
 * @Description:
 */
@Controller
public class OrderController {

    @Resource
    private OrderFeignClient orderFeignClient;

    @GetMapping("/trade.html")
    public String trade(Model model) {
        //detailArrayList totalNum totalAmount
        Result<Map<String, Object>> mapResult = orderFeignClient.authTrade();
        model.addAllAttributes(mapResult.getData());
        return "order/trade";
    }
}
