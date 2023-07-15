package com.atguigu.gmall.product.demo;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.product.service
 * class:TestService
 *
 * @author: smile
 * @create: 2023/7/13-14:25
 * @Version: v1.0
 * @Description:
 */
public interface TestService {
    void testLock();
    /**
     * 读数据接口
     * @return
     */
    String read();

    /**
     * 写数据接口
     * @return
     */
    void write();
}
