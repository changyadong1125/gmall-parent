package com.atguigu.gmall.item.service.imp;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Service;



import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.item.service.imp
 * class:ItemServiceImp
 *
 * @author: smile
 * @create: 2023/7/11-16:29
 * @Version: v1.0
 * @Description:
 */
@Service
public class ItemServiceImp implements ItemService {
    @Resource
    private ProductFeignClient productFeignClient;
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取商品详情数据
     */
    @Override
    public Map<String, Object> getItem(Long skuId) {
        //获取是skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        HashMap<String, Object> map = new HashMap<>();
        //获取
        Map<Object, Object> skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        String jsonString = JSON.toJSONString(skuValueIdsMap);


        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
        List<HashMap<String, Object>> skuAttrList = attrList.stream().map(baseAttrInfo -> {
            HashMap<String, Object> map1 = new HashMap<>();
            map1.put("attrName", baseAttrInfo.getAttrName());
            map1.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
            return map1;
        }).collect(Collectors.toList());

        map.put("skuInfo", skuInfo);
        map.put("categoryView", productFeignClient.getCategoryView(skuInfo.getCategory3Id()));
        map.put("price", productFeignClient.getSkuPrice(skuId));
        map.put("spuPosterList", productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId()));
        map.put("spuSaleAttrList", productFeignClient.getSpuSaleAttrListCheckBySku(skuId,skuInfo.getSpuId()));
        map.put("valuesSkuJson",jsonString);
        map.put("skuAttrList",skuAttrList);
        return map;
    }
}
