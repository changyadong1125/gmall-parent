package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.user.service
 * class:UserAddressService
 *
 * @author: smile
 * @create: 2023/7/22-14:17
 * @Version: v1.0
 * @Description:
 */
public interface UserAddressService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取用户收货地址
     */
    List<UserAddress> getUserAddressListByUserId(String userId);
}
