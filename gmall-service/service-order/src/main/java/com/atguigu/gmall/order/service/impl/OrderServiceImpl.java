package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.Order;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.order.service.impl
 * class:OrderServiceImpl
 *
 * @author: smile
 * @create: 2023/7/22-15:43
 * @Version: v1.0
 * @Description:
 */
@Service
@RefreshScope
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RabbitService rabbitService;
    @Value("${ware.url}")
    private String WARE_URL;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo saveOrderInfo(OrderInfo orderInfo) {
        //  total_amount order_status user_id out_trade_no trade_body operate_time expire_time process_status
        //  给以上字段进行赋值.
        //调用该方法之前必须给赋值订单明细集合  orderInfo和orderDetailList有数据，从前端页面传过来
        orderInfo.sumTotalAmount();
        //开始下订单的时候订单状态应该是未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //第三方支付时需要使用的交易编号 不能重复必须要唯一
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //订单的主题描述
        orderInfo.setTradeBody("硅谷商店");
        //赋值当前的操作时间
        orderInfo.setOperateTime(new Date());
        //赋值当前订单的过期时间 可以根据用户的id  或者商品的库存剩余数量判断 或者给一个固定的过期时间
        //这个日期计算是线程不安全的
        //Calendar calendar = Calendar.getInstance();
        //calendar.add(Calendar.DATE,1);
        //localDateTime是线程安全的 或者还可以使用localDate（只可以操作日期） localTime jdk1.8全部是线程安全的
        Instant instant = LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
        //将instant变成date对象
        Date date = Date.from(instant);
        orderInfo.setExpireTime(date);
        //订单进度状态
        orderInfo.setProcessStatus(OrderStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            orderDetailList.forEach(orderDetail -> {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insert(orderDetail);
            });
        }
        return orderInfo;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:生成流水号
     */
    @Override
    public String getTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        //定义缓存key 使用string类型
        String tradeNoKey = userId + ":tradeNo";
        this.redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:检查流水号 防止回退无刷新重复提交
     */
    @Override
    public boolean checkTradeNo(HttpServletRequest request, String userId) {
        // 防止用户回退无刷新重复提交
        //获取流水号
        String tradeNo = request.getParameter("tradeNo");
        //删除流水号 多线程方式下会有安全问题 后面可以使用lua脚本解决
        String tradeNoKey = userId + ":tradeNo";
        String scriptText = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                "then\n" +
                "    return redis.call(\"del\",KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(scriptText);
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(tradeNoKey), tradeNo);
        return null != result && result == 0;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:检查库存
     */
    @Override
    public Boolean checkStock(Long skuId, Integer skuNum) {
        //把可能会发生变化的地址通常写在配置文件中
        //采用httpClient进行远程调用
        String result = HttpClientUtil.doGet(WARE_URL + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:订单列表
     */
    @Override
    public IPage<OrderInfo> getOrderPage(Page<OrderInfo> orderInfoPage, String userId, String orderStatus) {
        IPage<OrderInfo> orderPage = orderInfoMapper.getOrderPage(orderInfoPage, userId, orderStatus);
        orderPage.getRecords().forEach(orderInfo -> {
            orderInfo.setOrderStatusName(OrderStatus.getStatusNameByStatus(orderInfo.getOrderStatus()));
        });
        return orderPage;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    @Override
    public OrderInfo getOrderInfoByUserIdAndOrderId(Long userId, Long orderId) {
        LambdaQueryWrapper<OrderInfo> orderInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderInfoLambdaQueryWrapper.eq(OrderInfo::getId, orderId);
        orderInfoLambdaQueryWrapper.eq(OrderInfo::getUserId, userId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(orderInfoLambdaQueryWrapper);
        if (null != orderInfo) {
            orderInfo.setOrderDetailList(orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId)));
        }
        return orderInfo;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取订单信息
     */
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (null != orderInfo) {
            orderInfo.setOrderDetailList(orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId)));
        }
        return orderInfo;
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:取消订单
     */
    @Override
    public void execExpiredOrder(Long orderId) {
        this.updateOrderStatus(orderId, ProcessStatus.CLOSED);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更改订单状态
     */
    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfo.setProcessStatus(processStatus.name());
        baseMapper.updateById(orderInfo);
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:
     */
    public void sendDeductStockMsg(Long orderId) {
        //  更新当前订单状态.
        this.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        //
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        //根据发送小心将orderInfo中的部分数据封map
        HashMap<String, Object> map = this.initWareJson(orderInfo);
        //发消息
        this.rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK, MqConst.ROUTING_WARE_STOCK, JSON.toJSONString(map));
    }

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:转成map
     */
    public HashMap<String, Object> initWareJson(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        //  专门给拆单使用。
        map.put("wareId", orderInfo.getWareId());
        //获取订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            List<HashMap<String, Object>> detailList = orderDetailList.stream().map(orderDetail -> {
                HashMap<String, Object> detailMap = new HashMap<>();
                detailMap.put("skuId", orderDetail.getSkuId());
                detailMap.put("skuNum", orderDetail.getSkuNum());
                detailMap.put("skuName", orderDetail.getSkuName());
                return detailMap;
            }).collect(Collectors.toList());
            //details
            map.put("details", detailList);
        }
        return map;
    }
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:拆单
     */
    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        //创建一个子订单集合
        ArrayList<OrderInfo> orderInfos = new ArrayList<>();
        //获取原始订单
        OrderInfo orderInfo = this.getOrderInfo(Long.valueOf(orderId));
        //根据字符串判断怎么拆单
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        if (!CollectionUtils.isEmpty(maps)) {
            for (Map map : maps) {
                String wareId = (String) map.get("wareId");
                List<String> skuIdsList = (List<String>) map.get("skuIds");
                //创建子订单并赋值
                OrderInfo subOrderInfo = new OrderInfo();
                BeanUtils.copyProperties(orderInfo, subOrderInfo);
                //防止主键冲突
                subOrderInfo.setId(null);
                //设置父级Id
                subOrderInfo.setParentOrderId(Long.parseLong(orderId));
                //设置仓库Id
                subOrderInfo.setWareId(wareId);
                //单独计算订单总金额
                List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList().stream().filter(orderDetail ->
                        skuIdsList.contains(orderDetail.getSkuId().toString())
                ).collect(Collectors.toList());
                subOrderInfo.setOrderDetailList(orderDetailList);
                //计算订单总金额
                subOrderInfo.sumTotalAmount();
                //将子订单添加到集合
                orderInfos.add(subOrderInfo);
                //姜子订单保存到数据库
                this.saveOrderInfo(subOrderInfo);
            }
        }
        //修改原始订单
        this.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.SPLIT);
        return orderInfos;
    }
}
