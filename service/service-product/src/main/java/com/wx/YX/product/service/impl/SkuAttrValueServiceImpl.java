package com.wx.YX.product.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wx.YX.model.product.SkuAttrValue;
import com.wx.YX.product.mapper.SkuAttrValueMapper;
import com.wx.YX.product.service.SkuAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * spu属性值 服务实现类
 * </p>
 *
 * @author meng
 * @since 2024-07-28
 */
@Service
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValue> implements SkuAttrValueService {

    @Override
    public List<SkuAttrValue> findBySkuId(Long skuId) {
        LambdaQueryWrapper<SkuAttrValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuAttrValue::getSkuId,skuId);

        return this.list(wrapper);
    }
}
