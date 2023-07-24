package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

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
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存订单
     */
    @Override
    public Long saveOrderInfo(OrderInfo orderInfo) {
        orderInfoMapper.insert(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)){
            orderDetailList.forEach(orderDetail -> {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insert(orderDetail);
            });
        }
        return orderInfo.getId();
    }
}
