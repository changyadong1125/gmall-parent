package com.atguigu.gmall.user.client.imp;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.LoginVo;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.client.imp
 * class:ProductDegradeFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-15:41
 * @Version: v1.0
 * @Description:
 */
@Component
public class UserDegradeFeignClient implements UserFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:用户登录
     */
    @Override
    public Result<Map<String, String>> login(LoginVo loginVo, HttpServletRequest request) {
        return null;
    }
}
