package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.mapper
 * class:BaseAttrInfoMapper
 *
 * @author: smile
 * @create: 2023/7/5-14:30
 * @Version: v1.0
 * @Description:
 */
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据分类Id查询平台属性集合
     */
    List<BaseAttrInfo> getAttrInfoList(@Param("category1Id") Long category1Id, @Param("category2Id")Long category2Id,@Param("category3Id") Long category3Id1);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加属性
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
}
