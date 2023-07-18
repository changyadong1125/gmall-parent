package com.atguigu.gmall;

import com.atguigu.gmall.common.constant.RedisConst;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.annotation.Resource;

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
public class ProductApp implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(ProductApp.class, args);
    }

    @Resource
    private RedissonClient redissonClient;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:启动的时候 就会执行该方法 初始化布隆过滤器  设置布隆过滤器的数据规模 和误判率
     */
    @Override
    public void run(String... args) throws Exception {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        bloomFilter.tryInit(500000,0.00001);
    }
}
