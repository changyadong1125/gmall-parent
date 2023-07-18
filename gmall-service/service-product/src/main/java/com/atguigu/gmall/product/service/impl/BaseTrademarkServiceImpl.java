package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @author HUAWEI
 * @description 针对表【base_trademark(品牌表)】的数据库操作Service实现
 * @createDate 2023-07-06 11:25:28
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {
    @Resource
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:查询品牌分页
     * 构建排序条件 构建Ipage
     */
    @Override
    public IPage<BaseTrademark> getBaseTrademarkPage(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<BaseTrademark> baseTrademarkLambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseTrademarkLambdaQueryWrapper.orderByDesc(BaseTrademark::getId);
        IPage<BaseTrademark> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, baseTrademarkLambdaQueryWrapper);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据品牌Id查询品牌信息
     */
    @Override
    public BaseTrademark getTrademarkById(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }
}




