package com.wx.YX.search.service;

public interface SkuService {
    //上架
    void upperSku(Long skuId);

    //下架
    void lowerSku(Long skuId);
}
