package com.atguigu.gmall.mapper;

import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.mapper
 * class:BaseAttrValueMapper
 *
 * @author: smile
 * @create: 2023/7/5-20:26
 * @Version: v1.0
 * @Description:
 */
public interface BaseAttrValueMapper extends BaseMapper<BaseAttrValue> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加属性值
     */
    void saveAttrValue(BaseAttrValue baseAttrValue);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据属性Id获取属性值列表
     */
    List<BaseAttrValue> getAttrValueList(@Param("attrId") Long attrId);
}
