package com.atguigu.gmall.service;


import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author HUAWEI
* @description 针对表【base_category_trademark(三级分类表)】的数据库操作Service
* @createDate 2023-07-06 11:26:25
*/
public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id获取品牌列表
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id获取当前可以添加的所有品牌列表
     */
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:新增当前三级分类品牌
     */
    void saveTrademark(CategoryTrademarkVo categoryTrademarkVo);
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id和品牌Id删除品牌
     */
    void removeTrademark(Long category3Id, Long id);
}
