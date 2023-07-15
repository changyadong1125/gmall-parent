package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import javax.crypto.spec.OAEPParameterSpec;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Resource
    private SkuAttrValueMapper skuAttrValueMapper;
    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Resource
    private RedissonClient redissonClient;

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
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        if (!StringUtils.isEmpty(skuInfo.getId())) {
            skuInfoMapper.updateById(skuInfo);
            skuImageService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, skuInfo.getId()));
            skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, skuInfo.getId()));
            skuSaleAttrValueService.remove(new LambdaQueryWrapper<SkuSaleAttrValue>().eq(SkuSaleAttrValue::getSkuId, skuInfo.getId()));
        } else {
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
            skuImageList = skuImageList.stream()
                    .peek(skuImage -> skuImage.setSkuId(skuInfo.getId())).collect(Collectors.toList());
            skuImageService.saveBatch(skuImageList);
        }
        //保存sku平台属性集合
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList = skuAttrValueList.stream()
                    .peek(skuAttrValue -> skuAttrValue.setSkuId(skuInfo.getId())).collect(Collectors.toList());
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            });
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);
            //将新增的商品SkuID存入布隆过滤器
            //5. 获取布隆过滤器，将新增skuID存入布隆过滤器
            RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
            bloomFilter.add(skuInfo.getId());
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
        //将新增的商品SkuID存入布隆过滤器
        //5. 获取布隆过滤器，将新增skuID存入布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        bloomFilter.add(skuId);
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
        skuInfoMapper.update(new SkuInfo(), new LambdaUpdateWrapper<SkuInfo>()
                .eq(SkuInfo::getId, skuId).set(SkuInfo::getIsSale, 0));
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取商品的最新价格
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return skuInfoService.getObj(new LambdaQueryWrapper<SkuInfo>().eq(SkuInfo::getId, skuId)
                .select(SkuInfo::getPrice), V -> (BigDecimal) V);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId获取平台属性和平台属性值
     */
    @Override
    @GmallCache(prefix = "attrList")
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return skuAttrValueMapper.getAttrList(skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据spuId 获取到销售属性值Id 与skuId 组成的数据集
     * Map 可以代替实体类  本质是key value
     */
    @Override
    @GmallCache(prefix = "skuValueIdsMap")
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        List<Map<Object, Object>> mapList = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        HashMap<Object, Object> resultMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(mapList)) {
            mapList.forEach(map -> resultMap.put(map.get("value_ids"), map.get("sku_id")));
        }
        return resultMap;
    }
}
