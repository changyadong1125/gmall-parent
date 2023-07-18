package com.atguigu.gmall.list.service;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.list.service.imp
 * class:SearchService
 *
 * @author: smile
 * @create: 2023/7/17-15:19
 * @Version: v1.0
 * @Description:
 */
public interface SearchService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品上架 传到es
     */
    void upperGoods(Long skuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品从es中删除
     */
    void lowerGoods(Long skuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新商品的热度排名分值
     */
    void incrHotScore(Long skuId);
}
