package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author HUAWEI
* @description 针对表【sku_attr_value(sku平台属性值关联表)】的数据库操作Mapper
* @createDate 2023-07-08 15:16:27
* @Entity com.atguigu.gmall.model.SkuAttrValue
*/
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取平台属性和平台属性值
     */
    List<BaseAttrInfo> getAttrList(Long skuId);
}




