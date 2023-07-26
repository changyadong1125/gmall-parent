package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;


/**
 * project:gmall-parent
 * package:com.atguigu.gmall.order.service
 * class:OrderService
 *
 * @author: smile
 * @create: 2023/7/22-15:41
 * @Version: v1.0
 * @Description:
 */
public interface OrderService extends IService<OrderInfo> {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存订单
     */
    OrderInfo saveOrderInfo(OrderInfo orderInfo);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取流水号
     */
    String getTradeNo(String userId);

    boolean checkTradeNo(HttpServletRequest request, String userId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:检查库存
     */
    Boolean checkStock(Long skuId, Integer skuNum);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:订单列表
     */
    IPage<OrderInfo> getOrderPage(Page<OrderInfo> orderInfoPage, String userId, String orderStatus);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取OrderInfo
     */
    OrderInfo getOrderInfoByUserIdAndOrderId(Long userId, Long orderId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:获取订单信息
     */
    OrderInfo getOrderInfo(Long orderId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:取消订单
     */
    void execExpiredOrder(Long orderId);

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:更改订单状态
     */
    void updateOrderStatus(Long orderId, ProcessStatus closed);
}
