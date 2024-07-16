package com.wx.YX.vo.order;

import com.wx.YX.enums.OrderStatus;
import com.wx.YX.enums.OrderStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderUserQueryVo {

	private Long userId;

	@ApiModelProperty(value = "订单状态")
	private OrderStatus orderStatus;

}

