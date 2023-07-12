package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.product.*;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service
 * class:MangeService
 *
 * @author: smile
 * @create: 2023/7/5-10:57
 * @Version: v1.0
 * @Description:
 */
public interface MangeService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取一级分类
     */
    List<BaseCategory1> getCategory1();

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取二级分类
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取三级分类
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据分类Id获取平台属性
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加平台属性
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据属性Id获取属性值列表
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据id获取平台属性
     */
    BaseAttrInfo getBaseAttrInfo(Long attrId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取基础销售属性列表
     */
    List<BaseSaleAttr> getBaseSaleAttrList();
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId查询SkuInfo和skuImage
     */
    SkuInfo getSkuInfo(Long skuId);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id查询分类数据
     */
    BaseCategoryView getCategoryView(Long category3Id);
}
