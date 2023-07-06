package com.atguigu.gmall.service;


import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author HUAWEI
* @description 针对表【base_trademark(品牌表)】的数据库操作Service
* @createDate 2023-07-06 11:25:28
*/
public interface BaseTrademarkService extends IService<BaseTrademark> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取品牌列表分页
     */
    IPage<BaseTrademark> getBaseTrademarkPage(Integer pageNum, Integer pageSize);
}
