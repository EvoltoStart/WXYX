package com.wx.YX.order.service;

import com.wx.YX.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.vo.order.OrderConfirmVo;
import com.wx.YX.vo.order.OrderSubmitVo;

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
}
