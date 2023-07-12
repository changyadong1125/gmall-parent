package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.service.impl
 * class:SpuManageServiceImp
 *
 * @author: smile
 * @create: 2023/7/7-19:35
 * @Version: v1.0
 * @Description:
 */
@Service
public class SpuManageServiceImp implements SpuManageService {

    @Resource
    private SpuInfoService spuInfoService;
    @Resource
    private SpuImageService spuImageService;
    @Resource
    private SpuPosterService spuPosterService;
    @Resource
    private SpuSaleAttrService spuSaleAttrService;
    @Resource
    private SpuSaleAttrValueService spuSaleAttrValueService;
    @Resource
    private SpuInfoMapper spuInfoMapper;
    @Resource
    private SpuImageMapper spuImageMapper;
    @Resource
    private SpuPosterMapper spuPosterMapper;
    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Resource
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类ID查询spu分页
     */
    @Override
    public IPage<SpuInfo> getSpuPage(IPage<SpuInfo> iPage, Long category3Id) {
        LambdaQueryWrapper<SpuInfo> spuInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuInfoLambdaQueryWrapper.eq(SpuInfo::getCategory3Id, category3Id)
                .orderByDesc(SpuInfo::getId);
        return spuInfoService.page(iPage, spuInfoLambdaQueryWrapper);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:添加spu
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //保存spuInfo信息
        spuInfoService.save(spuInfo);
        //保存spuImages
        if (!CollectionUtils.isEmpty(spuImageList)) {
            spuImageList = spuImageList.stream()
                    .peek(image -> image.setSpuId(spuInfo.getId())).collect(Collectors.toList());
            spuImageService.saveBatch(spuImageList);
        }
        //保存spuPosterList
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            spuPosterList = spuPosterList.stream()
                    .peek(poster -> poster.setSpuId(spuInfo.getId())).collect(Collectors.toList());
            spuPosterService.saveBatch(spuPosterList);
        }
        //保存spuSaleAttrList
        spuSaleAttrList.forEach(spuSaleAttr -> {
            spuSaleAttr.setSpuId(spuInfo.getId());
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            //保存spuSaleAttrValue
            if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                });
                spuSaleAttrValueService.saveBatch(spuSaleAttrValueList);
            }
        });
        spuSaleAttrService.saveBatch(spuSaleAttrList);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:回显spu
     */
    @Override
    public SpuInfo getSpuInfo(Long spuId) {
        SpuInfo spuInfo = spuInfoMapper.selectById(spuId);
        LambdaQueryWrapper<SpuImage> spuImageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuImageLambdaQueryWrapper.eq(SpuImage::getSpuId, spuId);
        spuInfo.setSpuImageList(spuImageMapper.selectList(spuImageLambdaQueryWrapper));

        LambdaQueryWrapper<SpuPoster> spuPosterLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuPosterLambdaQueryWrapper.eq(SpuPoster::getSpuId, spuId);
        spuInfo.setSpuPosterList(spuPosterMapper.selectList(spuPosterLambdaQueryWrapper));

        LambdaQueryWrapper<SpuSaleAttr> spuSaleAttrLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuSaleAttrLambdaQueryWrapper.eq(SpuSaleAttr::getSpuId, spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(spuSaleAttrLambdaQueryWrapper);
        spuSaleAttrList.forEach(spuSaleAttr -> {
            LambdaQueryWrapper<SpuSaleAttrValue> spuSaleAttrValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
            spuSaleAttrValueLambdaQueryWrapper.eq(SpuSaleAttrValue::getSpuId, spuId)
                    .eq(SpuSaleAttrValue::getBaseSaleAttrId, spuSaleAttr.getBaseSaleAttrId());
            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(spuSaleAttrValueLambdaQueryWrapper);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        });
        spuInfo.setSpuSaleAttrList(spuSaleAttrList);
        return spuInfo;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:修改spu
     */
    @Override
    @Transactional
    public void updateSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.updateById(spuInfo);
        //根据spuId和imagName删除图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        spuImageList.forEach(spuImage -> {
            LambdaQueryWrapper<SpuImage> spuImageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            spuImageLambdaQueryWrapper.eq(SpuImage::getSpuId, spuInfo.getId())
                    .eq(SpuImage::getImgName, spuImage.getImgName());
            spuImageMapper.delete(spuImageLambdaQueryWrapper);
            spuImage.setSpuId(spuInfo.getId());
        });
        //更新spuImageList
        spuImageService.saveBatch(spuImageList);
        //根据spuId和imagName删除海报
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        spuPosterList.forEach(spuPoster -> {
            LambdaQueryWrapper<SpuPoster> spuImageLambdaQueryWrapper = new LambdaQueryWrapper<>();
            spuImageLambdaQueryWrapper.eq(SpuPoster::getSpuId, spuInfo.getId())
                    .eq(SpuPoster::getImgName, spuPoster.getImgName());
            spuPosterMapper.delete(spuImageLambdaQueryWrapper);
            spuPoster.setSpuId(spuInfo.getId());
        });
        //更新spuPosterList
        spuPosterService.saveBatch(spuPosterList);
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        spuSaleAttrList.forEach(spuSaleAttr -> {
            //根据spuId和baseSaleAttrId删除SpuSaleAttr
            LambdaQueryWrapper<SpuSaleAttr> spuSaleAttrLambdaQueryWrapper = new LambdaQueryWrapper<>();
            spuSaleAttrLambdaQueryWrapper.eq(SpuSaleAttr::getSpuId, spuInfo.getId())
                    .eq(SpuSaleAttr::getBaseSaleAttrId, spuSaleAttr.getBaseSaleAttrId());
            spuSaleAttrMapper.delete(spuSaleAttrLambdaQueryWrapper);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            spuSaleAttr.setSpuId(spuInfo.getId());
            //根据spuId和baseSaleAttrId删删除spuSaleAttrValue
            spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                LambdaQueryWrapper<SpuSaleAttrValue> spuSaleAttrValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
                spuSaleAttrValueLambdaQueryWrapper.eq(SpuSaleAttrValue::getSpuId, spuInfo.getId())
                        .eq(SpuSaleAttrValue::getBaseSaleAttrId, spuSaleAttr.getBaseSaleAttrId());
                spuSaleAttrValueMapper.delete(spuSaleAttrValueLambdaQueryWrapper);
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
            });
            //更新spuSaleAttrValueList
            spuSaleAttrValueService.saveBatch(spuSaleAttrValueList);
        });
        //更新spuSaleAttrList
        spuSaleAttrService.saveBatch(spuSaleAttrList);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId获取spu消费属性列表
     */
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        LambdaQueryWrapper<SpuSaleAttr> spuSaleAttrLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuSaleAttrLambdaQueryWrapper.eq(SpuSaleAttr::getSpuId, spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(spuSaleAttrLambdaQueryWrapper);
        return spuSaleAttrList.stream().peek(spuSaleAttr -> {
            LambdaQueryWrapper<SpuSaleAttrValue> spuSaleAttrValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
            spuSaleAttrValueLambdaQueryWrapper.eq(SpuSaleAttrValue::getSpuId, spuId)
                    .eq(SpuSaleAttrValue::getBaseSaleAttrId, spuSaleAttr.getBaseSaleAttrId());
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueMapper.selectList(spuSaleAttrValueLambdaQueryWrapper));
        }).collect(Collectors.toList());
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId获取spu图片列表
     */
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        LambdaQueryWrapper<SpuImage> spuImageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spuImageLambdaQueryWrapper.eq(SpuImage::getSpuId, spuId);
        return spuImageMapper.selectList(spuImageLambdaQueryWrapper);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取海报数据
     */
    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        return spuPosterMapper.selectList(new LambdaQueryWrapper<SpuPoster>().eq(SpuPoster::getSpuId, spuId));
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId,skuId 获取销售属性数据
     */
    @Override
    public  List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return  spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }
}
