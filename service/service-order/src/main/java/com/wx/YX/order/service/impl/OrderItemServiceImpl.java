package com.wx.YX.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.model.order.OrderItem;
import com.wx.YX.order.mapper.OrderItemMapper;
import com.wx.YX.order.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}
