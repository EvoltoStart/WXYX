package com.wx.YX.search.service;

import com.wx.YX.model.search.SkuEs;

import java.util.List;

public interface SkuService {
    //上架
    void upperSku(Long skuId);

    //下架
    void lowerSku(Long skuId);

    List<SkuEs> findHotSkuList();
}
