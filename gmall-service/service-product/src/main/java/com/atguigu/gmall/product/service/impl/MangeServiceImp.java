package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.MangeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private SkuImageMapper skuImageMapper;
    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId查询SkuInfo和skuImage 整合缓存
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = null;
        //声明一个缓存key
        String skuInfoKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        //判断使用那种数据类型 存储对象最常用地使用Hash 便于对属性的修改
        try {
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuInfoKey);
            if (null == skuInfo) {
                //缓存中没有数据 加锁 redis + lua 声明一个锁
                String locKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                String token = UUID.randomUUID().toString();
                Boolean result = this.redisTemplate.opsForValue().setIfAbsent(locKey, token, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (Boolean.TRUE.equals(result)) {
                    //进行锁续期
                    Thread thread = new Thread(() -> {
                        Long expire = this.redisTemplate.getExpire(locKey);
                        if (expire != null && expire <= RedisConst.SKULOCK_EXPIRE_PX2 / 3L) {
                            this.redisTemplate.expire(locKey, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                        }
                    });
                    thread.setDaemon(true);
                    thread.start();
                    //获取到锁再去缓存中查询一遍
                    skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuInfoKey);
                    //如果缓存不为空 直接返回
                    if (null != skuInfo) {
                        return skuInfo;
                    }
                    //如果缓存还是空 查数据库
                    skuInfo = this.getSkuInfoByDB(skuId);
                    //如果数据库为空 缓存中缓存一个空对象 过期时间设置短一些
                    if (null == skuInfo) {
                        SkuInfo skuInfo1 = new SkuInfo();
                        this.redisTemplate.opsForValue().set(skuInfoKey, skuInfo1, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfo1;
                    }
                    //走到这里说明数据库不为空 将数据库中的数据缓存到redis
                    this.redisTemplate.opsForValue().set(skuInfoKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    //使用lua脚本 释放锁
                    String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                            "then\n" +
                            "return redis.call(\"del\",KEYS[1])\n" +
                            "else\n" +
                            "return 0\n" +
                            "end";
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setScriptText(script);
                    redisScript.setResultType(Long.class);
                    //判断当前释放锁线程是不是拥有锁线程
                    while (token.equals(this.redisTemplate.opsForValue().get(locKey))) {
                        Long execute = this.redisTemplate.execute(redisScript, Collections.singletonList(locKey), token);
                        if (execute == null || execute == 0) {
                            this.redisTemplate.delete(locKey);
                        }
                    }
                } else {
                    Thread.sleep(300);
                    this.getSkuInfo(skuId);
                }
                //缓存中有数据直接返回
            } else {
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //发生异常 从数据库中查询并返回
        return this.getSkuInfoByDB(skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据skuId查询SkuInfo和skuImage
     */
    public SkuInfo getSkuInfoByDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo != null) {
            List<SkuImage> skuImageList = skuImageMapper.selectList(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, skuId));
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:根据三级分类Id查询分类数据
     */
    @Override
    @GmallCache(prefix = "categoryView")
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取分类集合列表
     */
    @Override
    @GmallCache(prefix = "baseCategoryList")
    public List<JSONObject> getBaseCategoryList() {
        //创建容器
        ArrayList<JSONObject> list = new ArrayList<>();
        //获取三级分类所有数据
        List<BaseCategoryView> baseCategoryViewList = this.baseCategoryViewMapper.selectList(null);
        //根据category1Id进行分组
        Map<Long, List<BaseCategoryView>> listMap1 = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        Set<Map.Entry<Long, List<BaseCategoryView>>> entries1 = listMap1.entrySet();
        Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator1 = entries1.iterator();
        //定义序号index
        int index = 1;
        //遍历一级分类
        while (iterator1.hasNext()) {
            JSONObject category1 = new JSONObject();
            Map.Entry<Long, List<BaseCategoryView>> entry1 = iterator1.next();
            Long category1Id = entry1.getKey();
            List<BaseCategoryView> categoryViewList1 = entry1.getValue();
            //封装一级分类数据
            category1.put("index", index);
            category1.put("categoryId", category1Id);
            category1.put("categoryName", categoryViewList1.get(0).getCategory1Name());
            //index 序号加一
            index++;
            //获取当前一级分类的二级分类目录和三级分类目录 根据二级分类Id分组
            Map<Long, List<BaseCategoryView>> listMap2 = categoryViewList1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            Set<Map.Entry<Long, List<BaseCategoryView>>> entries2 = listMap2.entrySet();
            Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator2 = entries2.iterator();
            //创建封装二级分类的容器
            ArrayList<JSONObject> list2 = new ArrayList<>();
            //遍历二级分类
            while (iterator2.hasNext()) {
                JSONObject category2 = new JSONObject();
                Map.Entry<Long, List<BaseCategoryView>> entry2 = iterator2.next();
                Long category2Id = entry2.getKey();
                List<BaseCategoryView> categoryViewList2 = entry2.getValue();
                category2.put("categoryId", category2Id);
                category2.put("categoryName", categoryViewList2.get(0).getCategory2Name());
                //遍历封装三级分类
                List<JSONObject> list3 = categoryViewList2.stream().map(baseCategoryView -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId", baseCategoryView.getCategory3Id());
                    category3.put("categoryName", baseCategoryView.getCategory3Name());
                    return category3;
                }).collect(Collectors.toList());
                //将三级分类放入二级分类
                category2.put("categoryChild", list3);
                //将二级分类添加到容器
                list2.add(category2);
            }
            //将二级数据放入一级分类
            category1.put("categoryChild", list2);
            //将一级分类添加到容器
            list.add(category1);
        }
        //返回容器
        return list;
    }
}
