package com.atguigu.gmall.service;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service
 * class:SpuManageService
 *
 * @author: smile
 * @create: 2023/7/7-19:34
 * @Version: v1.0
 * @Description:
 */
public interface SpuManageService{
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id分页查询spu
     */
    IPage<SpuInfo> getSpuPage(IPage<SpuInfo> iPage, Long category3Id);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加spu
     */
    void saveSpuInfo(SpuInfo spuInfo);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:回显spu
     */
    SpuInfo getSpuInfo(Long spuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改spu
     */
    void updateSpuInfo(SpuInfo spuInfo);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId获取spu销售属性列表
     */
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId获取spu图片列表
     */
    List<SpuImage> spuImageList(Long spuId);
}
