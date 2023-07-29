package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import javax.annotation.Resource;
import java.util.*;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.web.controller
 * class:ListController
 *
 * @author: smile
 * @create: 2023/7/19-8:21
 * @Version: v1.0
 * @Description:
 */
@Controller
public class ListController {
    @Resource
    private ListFeignClient listFeignClient;


    @GetMapping("/list.html")
    public String search(SearchParam searchParam, Model model) {
        //urlParam制作
        String urlParam = this.makeUrlParam(searchParam);
        //品牌面包屑制作
        String trademarkParam = this.makeTrademarkParam(searchParam.getTrademark());
        //平台属性面包屑
        List<SearchAttr> propsParamList = this.makePropsList(searchParam.getProps());
        //创建排序对象
        Map<String,String> orderMap = this.makeOrderMap(searchParam.getOrder());
        //获取远程数据
        Result<SearchResponseVo> result = listFeignClient.search(searchParam);
        model.addAllAttributes(this.packageMap(result.getData()));
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("trademarkParam", trademarkParam);
        model.addAttribute("propsParamList", propsParamList);
        model.addAttribute("orderMap", orderMap);

        return "list/index";
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:创建排序对象
     */
    private Map<String,String> makeOrderMap(String order) {
        //创建一个对象
        Map<String,String> map = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {
            String[] split = order.split(":");
            if (split.length == 2) {
                map.put("type", split[0]);
                map.put("sort", split[1]);
            }
        } else {
            map.put("type", "1");
            map.put("sort", "desc");
        }
        return map;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:平台属性面包屑
     */
    private List<SearchAttr> makePropsList(String[] props) {
        //声明一个容器
        List<SearchAttr> searchAttrList = new ArrayList<>();
        if (null != props && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split.length == 3) {
                    SearchAttr searchAttr = new SearchAttr();
                    searchAttr.setAttrId(Long.valueOf(split[0]));
                    searchAttr.setAttrValue(split[1]);
                    searchAttr.setAttrName(split[2]);
                    searchAttrList.add(searchAttr);
                }
            }
        }
        return searchAttrList;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:封装map
     */
    private Map<String, Object> packageMap(SearchResponseVo searchResponseVo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("trademarkList", searchResponseVo.getTrademarkList());
        map.put("attrsList", searchResponseVo.getAttrsList());
        map.put("goodsList", searchResponseVo.getGoodsList());
        map.put("total", searchResponseVo.getTotal());
        map.put("pageNo", searchResponseVo.getPageNo());
        map.put("pageSize", searchResponseVo.getPageSize());
        map.put("totalPages", searchResponseVo.getTotalPages());
        return map;
    }

    private String makeTrademarkParam(String trademark) {
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split.length == 2) {
                return "品牌:" + split[1];
            }
        }
        return "";
    }

    @SuppressWarnings("all")
    private String makeUrlParam(SearchParam searchParam) {
        //创建对象
        StringBuffer stringBuffer = new StringBuffer();
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            stringBuffer.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            stringBuffer.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            stringBuffer.append("category1Id=").append(searchParam.getCategory1Id());
        }
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            stringBuffer.append("keyword=").append(searchParam.getKeyword());
        }
        //判断用户是否根据品牌过滤
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            if (stringBuffer.length() > 0) {
                stringBuffer.append("&trademark=").append(searchParam.getTrademark());
            }
        }
        //判断用户是否根据平台属性检索
        String[] props = searchParam.getProps();
        if (null != props && props.length > 0) {
            for (String prop : props) {
                if (stringBuffer.length() > 0) {
                    stringBuffer.append("&props=").append(prop);
                }
            }
        }
        return "list.html?" + stringBuffer;
    }
}
