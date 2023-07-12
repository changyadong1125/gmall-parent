package com.atguigu.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.demo.controller
 * class:indexController
 *
 * @author: smile
 * @create: 2023/7/10-11:22
 * @Version: v1.0
 * @Description:
 */
@Controller
public class indexController {
    @GetMapping("index")
    public ModelAndView index(HttpServletRequest request, Map<String,String> map, Model model, ModelAndView modelAndView){
        map.put("name","99999999999");
//        map.put("id","111");
        List<String> strings = Arrays.asList("蔡徐坤", "成龙", "Mike");
        request.setAttribute("list",strings);
        request.setAttribute("age",11);
        request.setAttribute("id",11);
        request.getSession().setAttribute("session","session333");
        request.getSession().setAttribute("id",65);
        request.setAttribute("gname","<span style=color:green>绿色</span>");
        modelAndView.setViewName("index");
        modelAndView.addObject("12534",45678);
        modelAndView.addObject("ok",45678);
        modelAndView.addObject("today",new Date());
        return modelAndView;
    }
    @GetMapping("list")
    public String list( Long id){
        System.out.println(id);
        return "inner";
    }
}
