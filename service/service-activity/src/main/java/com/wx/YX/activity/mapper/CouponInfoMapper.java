package com.wx.YX.activity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wx.YX.model.activity.CouponInfo;
import feign.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 优惠券信息 Mapper 接口
 * </p>
 *
 * @author meng
 * @since 2024-08-01
 */
@Repository
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    List<CouponInfo> seletcCouponInfoList(@Param("skuId") Long id, @Param("categoryId") Long categoryId, @Param("userId") Long userId);

    List<CouponInfo> selectCartCouponInfoList(@Param("userId") Long userId);
}
