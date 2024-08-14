package com.wx.YX.payment.service;

import com.wx.YX.enums.PaymentType;
import com.wx.YX.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    /**
     * 保存交易记录
     * @param orderNo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */

   //添加支付记录
    PaymentInfo savePaymentInfo(String orderNo, PaymentType paymentType);
    //根据orderNo查询支付记录
    PaymentInfo getPaymentInfo(String orderNo, PaymentType paymentType);

    //支付成功
    void paySuccess(String orderNo,PaymentType paymentType, Map<String,String> paramMap);
}