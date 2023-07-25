package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * project:gmall-parent
 * package:com.atguigu.gmall.order.mapper
 * class:OrderInfoMapper
 *
 * @author: smile
 * @create: 2023/7/22-15:48
 * @Version: v1.0
 * @Description:
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    IPage<OrderInfo> getOrderPage(Page<OrderInfo> orderInfoPage, @Param("userId") String userId, @Param("orderStatus") String orderStatus);
}
