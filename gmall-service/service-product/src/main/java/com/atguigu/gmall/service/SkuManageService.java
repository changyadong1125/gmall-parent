package com.atguigu.gmall.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service
 * class:SkuManageService
 *
 * @author: smile
 * @create: 2023/7/8-15:12
 * @Version: v1.0
 * @Description:
 */
public interface SkuManageService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id查询spu列表
     */
    IPage<SkuInfo> getSkuPage(IPage<SkuInfo> iPage, Long category3Id);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存saveSkuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:回显skuInfo
     */
    SkuInfo getSkuInfo(Long id);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改sku
     */
    void updateSkuInfo(SkuInfo skuInfo);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品上架
     */
    void onSale(Long skuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品下架
     */
    void cancelSale(Long skuId);
}
