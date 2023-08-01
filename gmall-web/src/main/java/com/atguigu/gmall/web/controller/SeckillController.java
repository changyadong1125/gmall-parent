package com.atguigu.gmall.web.controller;
import com.atguigu.gmall.activity.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.web.controller
 * class:SeckillController
 *
 * @author: smile
 * @create: 2023/8/1-10:23
 * @Version: v1.0
 * @Description:
 */
@Controller
public class SeckillController {
    @Resource
    private ActivityFeignClient activityFeignClient;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    @GetMapping("seckill.html")
    public String index(Model model) {
        Result<List<SeckillGoods>> result = activityFeignClient.findAll();
        model.addAttribute("list", result.getData());
        return "seckill/index";
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    @GetMapping("seckill/{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model) {
        // 通过skuId 查询skuInfo
        Result<SeckillGoods> result = activityFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item", result.getData());
        return "seckill/item";
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:排队页面
     */
    @GetMapping("seckill/queue.html")
    public String queue(@RequestParam(name = "skuId") Long skuId,
                        @RequestParam(name = "skuIdStr") String skuIdStr,
                        Model model) {
        model.addAttribute("skuId", skuId);
        model.addAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:去下单页面
     */
    @GetMapping("seckill/trade.html")
    public String trade(Model model) {
        //在结算页面控制器中放入map中
        Result<Map<String, Object>> result = activityFeignClient.seckillTradeData();
        if (result.isOk()) {
            model.addAllAttributes(result.getData());
            return "seckill/trade";
        } else {
            model.addAttribute("message","恭喜你傻蛋，没抢到.....");
            return "seckill/fail";
        }
    }
}