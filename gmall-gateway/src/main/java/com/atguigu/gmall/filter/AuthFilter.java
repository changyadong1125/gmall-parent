package com.atguigu.gmall.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.annotation.Resource;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.filter
 * class:AuthFilter
 *
 * @author: smile
 * @create: 2023/7/21-11:22
 * @Version: v1.0
 * @Description:
 */
@Controller
public class AuthFilter implements GlobalFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Value("${authUrls.url}")
    private String authUrls;
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:过滤器
     * exchange web请求 xxx.js xxx.css
     * chain 过滤链
     */
    @Override
    @SuppressWarnings("all")
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //过滤掉静态资源的拦截
        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //判断是否匹配
        //获取请求路径
        String path = request.getURI().getPath();
        if (antPathMatcher.match("/**/css/**", path) || antPathMatcher.match("/**/js/**", path) || antPathMatcher.match("/**/img/**", path)) {
            chain.filter(exchange);
        }
        //判断用户是否访问了内部接口
        if (antPathMatcher.match("/**/inner/**", path)) {
            //禁止访问
            return this.out(response, ResultCodeEnum.PERMISSION);
        }
        //限制用户在未登录状态访问带有auth的路径
        String userId = this.getUserId(request);
        String userTempId = this.getUserTempId(request);
        if ("-1".equals(userId)) {
            return this.out(response, ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //判断匹配
        if (antPathMatcher.match("/**/auth/**", path)) {
            //判断用户是否登录
            if (StringUtils.isEmpty(userId)) {
                return this.out(response, ResultCodeEnum.LOGIN_AUTH);
            }

        }
        //判断用户访问按写业务室必须要的登录
        //trade.html myOrder.html  list.html
        String[] split = authUrls.split(",");
        if (split.length > 0) {
            for (String url : split) {
                //判断是否包含以上信息
                if (path.contains(url) && StringUtils.isEmpty(userId)) {
                    //未登录的情况下访问了需要登录的页面
                    //拦截跳转到登录页面
                    //设置响应
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    //设置重定向路径
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://passport.gmall.com/login.html?originUrl="+request.getURI());
                    //重定向
                    return response.setComplete();
                }
            }
        }
        //比较重要的操作 把用户id放入请求头
        if (!StringUtils.isEmpty(userId)||!StringUtils.isEmpty(userTempId)){

            if (!StringUtils.isEmpty(userId)){
                //将用户id村粗在request的header中
               request.mutate().header("userId", userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)){
                request.mutate().header("userTempId", userTempId).build();
            }
            //如果存在切放入成功 结束操作 不能使用exchange 因为里面没有userId 应该吧httpRequest封装exchange
            return chain.filter(exchange.mutate().request(request).build());
        }
        //默认走其他过滤器
        return chain.filter(exchange);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    private String getUserTempId(ServerHttpRequest request) {

        String userTempId = "";
        //1.尝试从cookie中获取
        List<HttpCookie> cookieList = request.getCookies().get("userTempId");
        if (!CollectionUtils.isEmpty(cookieList)) {
            userTempId = cookieList.get(0).getValue();
            return userTempId;
        }
        else {
            List<String> list = request.getHeaders().get("userTempId");
            if (!CollectionUtils.isEmpty(list)){
                userTempId=list.get(0);
            }
        }
        return userTempId;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取用户id
     */
    private String getUserId(ServerHttpRequest request) {
        //获取用户Id需要先获取token
        //token存储在cookie 或者header

        HttpCookie httpCookie = request.getCookies().getFirst("token");
        String token = "";
        if (null != httpCookie) {
            //获取token
            token = httpCookie.getValue();
        } else {
            List<String> list = request.getHeaders().get("token");
            if (!CollectionUtils.isEmpty(list)){
                token=list.get(0);
            }
        }
        if (!StringUtils.isEmpty(token)) {
            String loginKey = "user:login:" + token;
            String userJson =  this.redisTemplate.opsForValue().get(loginKey);
            if (!StringUtils.isEmpty(userJson)) {
                JSONObject userInfo = JSONObject.parseObject(userJson);
                if (null != userInfo) {
                    String ip = (String) userInfo.get("ip");
                    if (ip.equals(IpUtil.getGatwayIpAddress(request))) {
                        return userInfo.get("userId").toString();
                    } else {
                        return "-1";
                    }
                }
            }
        }
        return "";
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:响应方法
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        //提示信息
        /*String message  = resultCodeEnum.getMessage();
        DataBuffer wrap = response.bufferFactory().wrap(message.getBytes());*/
        Result<Object> result = Result.build(null, resultCodeEnum);
        //做响应
        DataBuffer wrap = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());
        //设置当前请求头类型     text/html 指一种页面格式
        response.getHeaders().add("content-Type","application/json;charset=utf-8");
        return response.writeWith(Mono.just(wrap));
    }
}
