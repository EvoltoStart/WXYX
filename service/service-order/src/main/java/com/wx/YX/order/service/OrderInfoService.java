package com.wx.YX.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.vo.order.OrderConfirmVo;
import com.wx.YX.vo.order.OrderSubmitVo;
import com.wx.YX.vo.order.OrderUserQueryVo;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @author meng
 * @since 2024-08-12
 */
public interface OrderInfoService extends IService<OrderInfo> {

    OrderConfirmVo confirmOrder();

    Long submitOrder(OrderSubmitVo orderParamVo);

    OrderInfo getOrderInfoById(Long orderId);

    OrderInfo getOrderInfoByOrderNo(String orderNo);

    void orderPay(String orderNo);

    IPage<OrderInfo> findUserOrderPage(Page<OrderInfo> pageParam, OrderUserQueryVo orderUserQueryVo);
}
