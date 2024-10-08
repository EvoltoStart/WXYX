package com.wx.YX.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wx.YX.model.product.SkuImage;
import com.wx.YX.product.mapper.SkuImageMapper;
import com.wx.YX.product.service.SkuImageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 商品图片 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
@Service
public class SkuImageServiceImpl extends ServiceImpl<SkuImageMapper, SkuImage> implements SkuImageService {

    @Override
    public List<SkuImage> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuImage::getSkuId, skuId);
        List<SkuImage> list = baseMapper.selectList(wrapper);
        return list;
    }
}
