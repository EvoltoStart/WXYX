package com.wx.YX.activity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wx.YX.model.activity.ActivityInfo;
import com.wx.YX.model.activity.ActivityRule;
import com.wx.YX.model.activity.ActivitySku;
import feign.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 活动表 Mapper 接口
 * </p>
 *
 * @author meng
 * @since 2024-08-01
 */
@Repository
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    List<Long> selectExistSkuIdList(@Param("skuIdList")List<Long> skuIdList);

    List<ActivityRule> selectActivityRuleList(@Param("skuId")Long skuId);

    List<ActivityRule> findActivityRule(@Param("skuId") Long skuId);

    List<ActivitySku> selectCartActivityList(@Param("skuIdList") List<Long> skuIdList);
}
