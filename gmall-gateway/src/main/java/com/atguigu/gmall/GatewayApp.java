package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall
 * class:GatewayApp
 *
 * @author: smile
 * @create: 2023/7/5-19:39
 * @Version: v1.0
 * @Description:
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApp.class, args);
    }
}
