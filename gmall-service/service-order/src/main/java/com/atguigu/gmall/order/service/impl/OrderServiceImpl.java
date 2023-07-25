package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
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
    public Long saveOrderInfo(OrderInfo orderInfo) {
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
        return orderInfo.getId();
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
        orderInfoLambdaQueryWrapper.eq(OrderInfo::getId,orderId);
        orderInfoLambdaQueryWrapper.eq(OrderInfo::getUserId,userId);
        return orderInfoMapper.selectOne(orderInfoLambdaQueryWrapper);
    }
}
