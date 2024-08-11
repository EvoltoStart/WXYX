package com.wx.YX.activity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.activity.ActivityInfo;
import com.wx.YX.model.activity.ActivityRule;
import com.wx.YX.model.order.CartInfo;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.vo.activity.ActivityRuleVo;
import com.wx.YX.vo.order.CartInfoVo;
import com.wx.YX.vo.order.OrderConfirmVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 活动表 服务类
 * </p>
 *
 * @author meng
 * @since 2024-08-01
 */
public interface ActivityInfoService extends IService<ActivityInfo> {

    IPage<ActivityInfo> selectPage(Page<ActivityInfo> pageParam);
    /**
     * 获取活动规则id
     * @param activityId
     * @return
     */
    Map<String, Object> findActivityRuleList(Long activityId);

    //保存活动规则信息
    void saveActivityRule(ActivityRuleVo activityRuleVo);

    //根据关键字获取sku信息列表
    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    /**
     * 根据skuId获取促销规则信息
     * @param skuId
     * @return
     */
    List<ActivityRule> findActivityRule(Long skuId);

    //根据skuId列表获取促销信息
    Map<Long, List<String>> findActivity(List<Long> skuIdList);

    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);

    //根据skuid获取活动规则信息
    List<ActivityRule> findActivityRuleListBySkuId(Long skuId);

    //获取购物车满足条件的促销与优惠券信息
    OrderConfirmVo findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);

    //获取购物项对应规则
    List<CartInfoVo> findCartActivityList(List<CartInfo> cartInfoList);
}
