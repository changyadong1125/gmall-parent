package com.atguigu.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall
 * class:ProductApp
 *
 * @author: smile
 * @create: 2023/7/4-19:25
 * @Version: v1.0
 * @Description:
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.atguigu.gmall.product.mapper")
public class ProductApp {
    public static void main(String[] args) {
        SpringApplication.run(ProductApp.class, args);
    }
}
