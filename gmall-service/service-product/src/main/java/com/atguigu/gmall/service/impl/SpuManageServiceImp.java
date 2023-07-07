package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.mapper.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
        spuImageList.forEach(image -> {
            image.setSpuId(spuInfo.getId());
        });
        spuImageService.saveBatch(spuImageList);

        //保存spuPosterList
        spuPosterList.forEach(poster -> {
            poster.setSpuId(spuInfo.getId());
        });
        spuPosterService.saveBatch(spuPosterList);
        //保存spuSaleAttrList
        spuSaleAttrList.forEach(saleAttr -> {
            saleAttr.setSpuId(spuInfo.getId());
            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setBaseSaleAttrId(saleAttr.getBaseSaleAttrId());
                spuSaleAttrValue.setSaleAttrValueName(saleAttr.getSaleAttrName());

            });
            spuSaleAttrValueService.saveBatch(spuSaleAttrValueList);
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
                spuSaleAttrValue.setSaleAttrValueName(spuSaleAttr.getSaleAttrName());
            });
            spuSaleAttrValueService.saveBatch(spuSaleAttrValueList);
        });
        spuSaleAttrService.saveBatch(spuSaleAttrList);

    }
}
