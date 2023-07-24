package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/userAddress")
public class UserApiController {
    @Resource
    private UserAddressService userAddressService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取用户收货地址
     */
    @GetMapping("/auth/findUserAddressList")
    public Result<?> findUserAddressList(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressList = userAddressService.getUserAddressListByUserId(userId);
        return Result.ok(userAddressList);
    }
}