package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author HUAWEI
* @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Mapper
* @createDate 2023-07-07 20:05:26
* @Entity com.atguigu.gmall.model.SpuSaleAttr
*/
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId,skuId 获取销售属性数据
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);
}




