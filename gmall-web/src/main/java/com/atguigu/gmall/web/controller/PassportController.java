package com.atguigu.gmall.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.web.controller
 * class:PassportController
 *
 * @author: smile
 * @create: 2023/7/20-14:33
 * @Version: v1.0
 * @Description:
 */
@Controller
public class PassportController {

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:登录页面
     */
    @GetMapping("login.html")
    public String login(HttpServletRequest request){
        //  存储数据
        request.setAttribute("originUrl",request.getParameter("originUrl"));
        //  返回登录页面
        return "login";
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:用户注册
     */
    @GetMapping("register.html")
    public String register(){


        return "register";
    }
}
