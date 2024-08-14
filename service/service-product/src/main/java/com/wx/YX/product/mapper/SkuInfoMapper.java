package com.wx.YX.product.mapper;

import com.wx.YX.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import feign.Param;

/**
 * <p>
 * sku信息 Mapper 接口
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    void unlockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    SkuInfo checkStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    Integer lockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);
}
