package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.LoginVo;
import com.atguigu.gmall.model.user.UserInfo;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.user.controller.service
 * class:UserInfoService
 *
 * @author: smile
 * @create: 2023/7/19-15:30
 * @Version: v1.0
 * @Description:
 */
public interface UserInfoService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:用户登录
     */
    UserInfo login(LoginVo loginVo);
}
