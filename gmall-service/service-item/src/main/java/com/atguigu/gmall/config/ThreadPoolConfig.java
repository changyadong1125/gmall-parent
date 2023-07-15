package com.atguigu.gmall.config;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.config
 * class:ThreadPoolExecutorConfig
 *
 * @author: smile
 * @create: 2023/7/15-15:50
 * @Version: v1.0
 * @Description:
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:自定义线程池
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        //动态获取服务器核数
        int processors = Runtime.getRuntime().availableProcessors();
        //  返回线程池对象
        return new ThreadPoolExecutor(
                processors + 1, // 核心线程个数 io:2n ,cpu: n+1  n:内核数据
                processors *2,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

}
