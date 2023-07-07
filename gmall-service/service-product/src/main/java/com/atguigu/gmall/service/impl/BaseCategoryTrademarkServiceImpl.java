package com.atguigu.gmall.service.impl;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.service.BaseCategoryTrademarkService;
import com.atguigu.gmall.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HUAWEI
 * @description 针对表【base_category_trademark(三级分类表)】的数据库操作Service实现
 * @createDate 2023-07-06 11:26:25
 */
@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {

    @Resource
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;
    @Resource
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id获取品牌列表
     */
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        return baseCategoryTrademarkMapper.findTrademarkList(category3Id);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id获取品牌列表 的另一种写法
     */
    public List<BaseTrademark> findTrademarkList_teacher(Long category3Id) {
        LambdaQueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkLambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategoryTrademarkLambdaQueryWrapper.eq(!StringUtils.isEmpty(category3Id), BaseCategoryTrademark::getCategory3Id, category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkLambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            List<Long> trademarkIdList = baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            return baseTrademarkMapper.selectBatchIds(trademarkIdList);
        }
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id获取当前可以添加的所有品牌列表
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        LambdaQueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkLambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategoryTrademarkLambdaQueryWrapper.eq(!StringUtils.isEmpty(category3Id), BaseCategoryTrademark::getCategory3Id, category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkLambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            //查询当前三级分类已有品牌Id
            List<Long> trademarkIdList = baseCategoryTrademarkList.stream()
                    .map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            //查询所有品牌Id
            LambdaQueryWrapper<BaseTrademark> baseTrademarkLambdaQueryWrapper = new LambdaQueryWrapper<>();
            baseTrademarkLambdaQueryWrapper.select(BaseTrademark::getId);
            List<Long> trademarksIdList = baseTrademarkMapper.selectObjs(baseTrademarkLambdaQueryWrapper).stream()
                    .map(obj -> (Long) obj).collect(Collectors.toCollection(ArrayList::new));
            //获取当前三级分类可添加Id
            List<Long> currenTrademarkIdList = trademarksIdList.stream()
                    .filter(id -> !trademarkIdList.contains(id)).collect(Collectors.toList());
            //根据当前Id列表查询品牌信息
            return baseTrademarkMapper.selectBatchIds(currenTrademarkIdList);
        }
        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:新增当前三级分类品牌
     */
    @Override
    public void saveTrademark(CategoryTrademarkVo categoryTrademarkVo) {
        categoryTrademarkVo.getTrademarkIdList().forEach(trademarkId -> {
            BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
            baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
            baseCategoryTrademark.setTrademarkId(trademarkId);
            baseCategoryTrademarkMapper.insert(baseCategoryTrademark);
        });
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级Id和品牌Id删除品牌
     */
    @Override
    public void removeTrademark(Long category3Id, Long id) {
        LambdaQueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkLambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategoryTrademarkLambdaQueryWrapper.eq(BaseCategoryTrademark::getCategory3Id, category3Id)
                .and(A->A.eq(BaseCategoryTrademark::getTrademarkId,id));
        baseCategoryTrademarkMapper.delete(baseCategoryTrademarkLambdaQueryWrapper);
    }
}




