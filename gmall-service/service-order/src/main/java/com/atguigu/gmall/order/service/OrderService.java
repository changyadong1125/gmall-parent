package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public interface OrderService {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:保存订单
     */
    Long saveOrderInfo(OrderInfo orderInfo);

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
}
