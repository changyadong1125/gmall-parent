package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.LoginVo;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "用户登录")
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Resource
    private RedisTemplate<String, ?> redisTemplate;

    @Resource
    private UserInfoService userInfoService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:用户登录
     */
    @PostMapping("/login")
    public Result<UserInfo> login(@RequestBody LoginVo loginVo, HttpServletRequest request) {
        return Result.ok(userInfoService.login(loginVo));
    }
}