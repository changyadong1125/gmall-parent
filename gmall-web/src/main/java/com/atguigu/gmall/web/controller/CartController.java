package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.web.controller
 * class:CartController
 *
 * @author: smile
 * @create: 2023/7/22-11:49
 * @Version: v1.0
 * @Description:
 */
@Controller
public class CartController {
    @Resource
    private ProductFeignClient productFeignClient;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查看购物车
     */
    @RequestMapping("cart.html")
    public String index(){
        return "cart/index";
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加购物车
     */
    @RequestMapping("addCart.html")
    public String addCart(@RequestParam(name = "skuId") Long skuId,
                          @RequestParam(name = "skuNum") Integer skuNum,
                          HttpServletRequest request){
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "cart/addCart";
    }
}
