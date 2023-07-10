package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.mapper.SkuInfoMapper;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jodd.util.StringUtil;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service.impl
 * class:SkuManageServiceImp
 *
 * @author: smile
 * @create: 2023/7/8-15:12
 * @Version: v1.0
 * @Description:
 */
@Service
public class SkuManageServiceImp implements SkuManageService {
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuImageService skuImageService;
    @Resource
    private SkuAttrValueService skuAttrValueService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类ID查询sku分页列表
     */
    @Override
    public IPage<SkuInfo> getSkuPage(IPage<SkuInfo> iPage, Long category3Id) {
        LambdaQueryWrapper<SkuInfo> skuInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        skuInfoLambdaQueryWrapper.eq(SkuInfo::getCategory3Id, category3Id).orderByDesc(SkuInfo::getId);
        return skuInfoMapper.selectPage(iPage, skuInfoLambdaQueryWrapper);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存saveSkuInfo
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        if (!StringUtils.isEmpty(skuInfo.getId())){
            skuInfoMapper.updateById(skuInfo);
            skuImageService.removeById(skuInfo.getId());
            skuAttrValueService.removeById(skuInfo.getId());
            skuSaleAttrValueService.removeById(skuInfo.getId());
        }else{
            //保存skuInfo
            skuInfoMapper.insert(skuInfo);
        }
        //获取sku图片集合
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        //获取sku平台属性值集合
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        //获取sku销售属性值集合
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //保存sku图片属性集合
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
            });
            skuImageService.saveBatch(skuImageList);
        }
        //保存sku平台属性集合
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
            });
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            });
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);
        }
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:回显skuInfo
     */
    @Override
    public SkuInfo getSkuInfo(Long id) {
        return skuInfoMapper.selectById(id);
    }

    @Override
    public void updateSkuInfo(SkuInfo skuInfo) {
       this.saveSkuInfo(skuInfo);
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品上架
     */
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
        //todo:
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品下架
     */
    @Override
    public void cancelSale(Long skuId) {
        LambdaUpdateWrapper<SkuInfo> skuInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        skuInfoLambdaUpdateWrapper.eq(SkuInfo::getId, skuId).set(SkuInfo::getIsSale,0);
        skuInfoMapper.update(new SkuInfo(),skuInfoLambdaUpdateWrapper);
    }
}
