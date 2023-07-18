package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.list.service.imp
 * class:SearchServiceImp
 *
 * @author: smile
 * @create: 2023/7/17-15:19
 * @Version: v1.0
 * @Description:
 */
@Service
@Slf4j
public class SearchServiceImp implements SearchService {
    @Resource
    private GoodsRepository goodsRepository;
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private RestHighLevelClient restHighLevelClient;


    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品上架 传到es
     */
    @Override
    public void upperGoods(Long skuId) {
        //布隆过滤器中查看是否有该商品
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)) {
            log.error("用户查询商品sku不存在：{}", skuId);
            //查询数据不存在直接返回空对象
            throw new GmallException("该商品不存在", 500);
        }
        //创建对象
        Goods goods = new Goods();
        goods.setId(skuId);
        //获取img
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync
                (() -> productFeignClient.getSkuInfo(skuId), threadPoolExecutor);

        CompletableFuture<Void> otherCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setCreateTime(new Date());
        }, threadPoolExecutor);

        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
        }, threadPoolExecutor);

        CompletableFuture<Void> trademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseTrademark trademark = this.productFeignClient.getTrademarkById(skuInfo.getTmId());
            //获取品牌数据
            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());
        }, threadPoolExecutor);

        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory3Name(categoryView.getCategory3Name());
        }, threadPoolExecutor);

        CompletableFuture<Void> SearchAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(searchAttrList);
        }, threadPoolExecutor);

        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                otherCompletableFuture,
                priceCompletableFuture,
                trademarkCompletableFuture,
                categoryViewCompletableFuture,
                SearchAttrCompletableFuture
        ).join();
        goodsRepository.save(goods);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:商品从es中删除
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更新商品的热度排名分值
     */
    @Override
    public void incrHotScore(Long skuId) {
        String key = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        if (score != null && score % 10 == 0) {
            Optional<Goods> optional = goodsRepository.findById(skuId);
            if (optional.isPresent()) {
                Goods goods = optional.get();
                goods.setHotScore(score.longValue());
                //更新es中的数据
                goodsRepository.save(goods);
            }
        }
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        //生成dsl语句 searchRequest 查询请求对象
        SearchRequest searchRequest = this.QueryBuildDsl(searchParam);
        //根据dsl语句查询结果
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.parseResult(searchResponse);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        //创建 SearchResponseVo
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //获取结果集
        SearchHits hits = searchResponse.getHits();

        //总记录数赋值
        searchResponseVo.setTotal(hits.getTotalHits().value);

        //给商品列表赋值
            //创建商品容器
            List<Goods> goodsList = new ArrayList<>();
            for (SearchHit searchHit : hits) {
                String sourceAsString = searchHit.getSourceAsString();
                Goods goods = JSONObject.parseObject(sourceAsString).toJavaObject(Goods.class);
                //如果是分词查询 goods的标题应该高亮显示
                if (searchHit.getHighlightFields().containsKey("title")) {
                    String title = searchHit.getHighlightFields().get("title").getFragments()[0].toString();
                    goods.setTitle(title);
                }
                //将Goods添加到商品容器中
                goodsList.add(goods);
            }
        searchResponseVo.setGoodsList(goodsList);

        //获取聚合数据
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //获取品牌数据
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) aggregationMap.get("attrIdAgg");

//        searchResponseVo.setTrademarkList();
//        searchResponseVo.setAttrsList();
//        searchResponseVo.setPageNo();
//        searchResponseVo.setPageSize();
//        searchResponseVo.setTotalPages();

        return null;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:生成dsl语句
     */
    private SearchRequest QueryBuildDsl(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //表示查询 query --> bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //判断用户是否根据分类Id进行过滤
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        //判断用户是否根据全文检索 bool -- must -- match
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
            //设置高亮规则 无检索不高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style=color:red>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        //判断用户是否通过品牌进行过滤
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            String[] split = searchParam.getTrademark().split(":");
            if (split.length == 2) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }
        //判断用户是否根据平台属性Id过滤 数据类型 nested
        String[] props = searchParam.getProps();
        if (null != props && props.length != 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split.length == 3) {
                    //创建内层bool
                    BoolQueryBuilder innerBoolQuery = QueryBuilders.boolQuery();
                    innerBoolQuery.filter(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    innerBoolQuery.filter(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                    //给外层的bool赋值
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", innerBoolQuery, ScoreMode.None));
                }
            }
        }
        //分页 pageNum-1*pageSize
        searchSourceBuilder.from((searchParam.getPageNo() - 1) * searchParam.getPageSize());
        searchSourceBuilder.size(searchParam.getPageSize());
        //排序 获取排序规则
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = order.split(":");
            if (split.length == 2) {
                searchSourceBuilder.sort("1".equals(split[0]) ? "hotScore" : "price", "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            } else {
                searchSourceBuilder.sort("hotScore", SortOrder.DESC);
            }
        }
        //品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        //平台属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")))
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")));

        //创建请求对象
        SearchRequest searchRequest = new SearchRequest("goods");
        //将查询器放入source中 指定获取那些数据
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price"}, null);
        //打印dsl语句
        System.out.println("dsl:\t" + searchSourceBuilder.toString());
        return searchRequest.source(searchSourceBuilder);
    }
}
