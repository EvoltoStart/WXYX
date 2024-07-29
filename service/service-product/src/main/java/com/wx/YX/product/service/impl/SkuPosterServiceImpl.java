package com.wx.YX.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wx.YX.model.product.SkuPoster;
import com.wx.YX.product.mapper.SkuPosterMapper;
import com.wx.YX.product.service.SkuPosterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 商品海报表 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
@Service
public class SkuPosterServiceImpl extends ServiceImpl<SkuPosterMapper, SkuPoster> implements SkuPosterService {

    @Override
    public List<SkuPoster> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuPoster> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SkuPoster::getSkuId,skuId);
        List<SkuPoster> list = this.list(wrapper);
        return list;
    }
}
