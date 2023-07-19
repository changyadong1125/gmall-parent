package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.LoginVo;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.user.controller.service.impl
 * class:UserInfoServiceImp
 *
 * @author: smile
 * @create: 2023/7/19-15:30
 * @Version: v1.0
 * @Description:
 */
@Service
public class UserInfoServiceImp implements UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    @Override
    public UserInfo login(LoginVo loginVo) {

        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getPasswd, MD5.encrypt(loginVo.getPasswd()));
        userInfoLambdaQueryWrapper.and(query -> {
            query.eq(UserInfo::getLoginName, loginVo.getLoginName()).or()
                    .eq(UserInfo::getEmail, loginVo.getLoginName()).or()
                    .eq(UserInfo::getPhoneNum, loginVo.getLoginName());
        });
      return  userInfoMapper.selectOne(userInfoLambdaQueryWrapper);
    }
}
