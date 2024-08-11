package com.wx.YX.activity.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.activity.CouponInfo;
import com.wx.YX.model.order.CartInfo;
import com.wx.YX.vo.activity.CouponRuleVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 优惠券信息 服务类
 * </p>
 *
 * @author meng
 * @since 2024-08-01
 */
public interface CouponInfoService extends IService<CouponInfo> {


    IPage<CouponInfo> selectpage(Long page, Long limit);

    CouponInfo getCouponInfo(String id);

    Map<String, Object> findCouponRuleList(Long id);

    void saveCouponRule(CouponRuleVo couponRuleVo);

    Object findCouponByKeyword(String keyword);

    List<CouponInfo> findCouponInfoList(Long skuId,Long userId);

    List<CouponInfo> findCartCouponInfo(List<CartInfo> cartInfoList, Long userId);

    CouponInfo findRangeSkuIdList(List<CartInfo> cartInfoList, Long couponId);
}
