package com.atguigu.gmall.filter;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.filter
 * class:CorsConfig
 *
 * @author: smile
 * @create: 2023/7/5-19:52
 * @Version: v1.0
 * @Description:
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 配置跨域规则
 *
 * @author: atguigu
 * @create: 2023-02-21 11:18
 */
@Configuration
public class CorsConfig {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:配置CorsWebFilter产生CORS过滤器,配置CORS跨域规则
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        //配置CORS
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1.配置允许访问域名
        corsConfiguration.addAllowedOrigin("*");
        //2.配置允许访问方式 POST DELETE GET
        corsConfiguration.addAllowedMethod("*");
        //3.配置允许提交头信息
        corsConfiguration.addAllowedHeader("*");
        //4.配置是否允许提交cookie
        corsConfiguration.setAllowCredentials(true);
        //5.配置预检请求有效时间
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //注册CORS配置
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
