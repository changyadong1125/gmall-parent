package com.atguigu.gmall.user.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.LoginVo;
import com.atguigu.gmall.user.client.imp.UserDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.client
 * class:ProductFeignClient
 *
 * @author: smile
 * @create: 2023/7/11-15:40
 * @Version: v1.0
 * @Description:
 */
@FeignClient(value = "service-user", path = "/api/user/passport", fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:用户登录
     */
    @PostMapping("/login")
    Result<Map<String, String>> login(@RequestBody LoginVo loginVo, HttpServletRequest request);
}
