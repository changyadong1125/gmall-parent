package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
* @author HUAWEI
* @description 针对表【sku_sale_attr_value(sku销售属性值)】的数据库操作Mapper
* @createDate 2023-07-08 15:16:27
* @Entity com.atguigu.gmall.model.SkuSaleAttrValue
*/
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * Map 可以代替实体类  本质是key value
     */
    List<Map<Object, Object>> getSkuValueIdsMap(Long spuId);
}




