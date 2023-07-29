package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.user.service.impl
 * class:UserAddressServiceImp
 *
 * @author: smile
 * @create: 2023/7/22-14:17
 * @Version: v1.0
 * @Description:
 */
@Service
public class UserAddressServiceImp implements UserAddressService {

    @Resource
    private UserAddressMapper userAddressMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取用户收货地址
     */
    @Override
    public List<UserAddress> getUserAddressListByUserId(String userId) {
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));
    }
}
