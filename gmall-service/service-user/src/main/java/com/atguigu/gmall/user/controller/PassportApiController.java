package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.LoginVo;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Api(tags = "用户登录")
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private UserInfoService userInfoService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:用户登录
     * 验证用户的用户名和密码  - 验证通过  -  将用户信息保存在redis中 键为用户姓名加Token  - 将token和用户需要展示的信息存到cookie
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginVo loginVo, HttpServletRequest request) {
        UserInfo userInfo = userInfoService.login(loginVo);
        if (userInfo == null) {
            return Result.fail();
        }
        //创建一个容器，用户给前端返回信息
        Map<String, String> map = new HashMap<>();
        //用户昵称
        String nickName = userInfo.getNickName();
        //生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        map.put("nickName", nickName);
        map.put("token", token);
        //定义键
        String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        //创建一个对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ip", IpUtil.getIpAddress(request));
        jsonObject.put("userId", userInfo.getId().toString());
        //将用户ip地址保存到redis
        this.redisTemplate.opsForValue().set(loginKey, jsonObject.toString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
        return Result.ok(map);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:退出登录
     */
    @GetMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = request.getHeader("token");
        String loginKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        Boolean result = this.redisTemplate.delete(loginKey);
        return Boolean.TRUE.equals(result) ? Result.ok() : Result.fail();
    }
}