package com.atguigu.gmall.mapper;


import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HUAWEI
 * @description 针对表【base_category_trademark(三级分类表)】的数据库操作Mapper
 * @createDate 2023-07-06 11:26:25
 * @Entity com.atguigu.gmall.model.BaseCategoryTrademark
 */
public interface BaseCategoryTrademarkMapper extends BaseMapper<BaseCategoryTrademark> {

    List<BaseTrademark> findTrademarkList(@Param("category3Id") Long category3Id);
}




