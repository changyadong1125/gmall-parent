package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.mapper.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.service.MangeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service.imp
 * class:MangeServiceImp
 *
 * @author: smile
 * @create: 2023/7/5-10:58
 * @Version: v1.0
 * @Description:
 */
@Service
public class MangeServiceImp implements MangeService {
    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;
    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;
    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;
    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Resource
    private BaseAttrValueMapper baseAttrValueMapper;
    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取一级分类
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取二级分类
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        LambdaQueryWrapper<BaseCategory2> baseCategory2LambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategory2LambdaQueryWrapper.eq(BaseCategory2::getCategory1Id, category1Id);
        return baseCategory2Mapper.selectList(baseCategory2LambdaQueryWrapper);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取三级分类
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        LambdaQueryWrapper<BaseCategory3> baseCategory3LambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategory3LambdaQueryWrapper.eq(BaseCategory3::getCategory2Id, category2Id);
        return baseCategory3Mapper.selectList(baseCategory3LambdaQueryWrapper);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据分类Id查询平台属性集合
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.getAttrInfoList(category1Id, category2Id, category3Id);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加和修改平台属性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (!StringUtils.isEmpty(baseAttrInfo.getId())) {
            baseAttrInfoMapper.deleteById(baseAttrInfo.getId());
            baseAttrValueMapper.deleteById(baseAttrInfo.getId());
        }
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList.size() != 0) {
            baseAttrInfoMapper.saveAttrInfo(baseAttrInfo);
            attrValueList.forEach(V -> {
                BaseAttrValue baseAttrValue = new BaseAttrValue();
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValue.setValueName(V.getValueName());
                baseAttrValueMapper.saveAttrValue(baseAttrValue);
            });
        }
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据属性Id获取属性值列表
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        return baseAttrValueMapper.getAttrValueList(attrId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据id查询属性，并设置属性值
     */
    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo != null) {
            baseAttrInfo.setAttrValueList(this.getAttrValueList(attrId));
            return baseAttrInfo;
        }
        return null;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }
}
