package com.wx.YX.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.product.SkuImage;

import java.util.List;

public interface SkuImageService extends IService<SkuImage> {
    List<SkuImage> findBySkuId(Long skuId);
}
