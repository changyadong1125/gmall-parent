package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atguigu.gmall.service.BaseTrademarkService;
import com.atguigu.gmall.mapper.BaseTrademarkMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * @author HUAWEI
 * @description 针对表【base_trademark(品牌表)】的数据库操作Service实现
 * @createDate 2023-07-06 11:25:28
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查询品牌分页
     */
    @Override
    public IPage<BaseTrademark> getBaseTrademarkPage(Integer pageNum, Integer pageSize) {
        IPage<BaseTrademark> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, null);
    }
}




