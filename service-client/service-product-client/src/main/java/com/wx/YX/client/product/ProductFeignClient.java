package com.wx.YX.client.product;


import com.wx.YX.model.product.Category;
import com.wx.YX.model.product.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
@FeignClient(value = "service-product")
public interface ProductFeignClient {
    @GetMapping("/api/product/inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable("categoryId") Long categoryId);
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);
    @PostMapping("/api/product/inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIdList);
    @PostMapping("/api/product/inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIds);

    @GetMapping("/api/product/inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable String keyword);

    //获取所有分类
    @GetMapping("/api/product/inner/findAllCategoryList")
    public List<Category> findAllCategoryList();
    //获取新人专享商品
    @GetMapping("/api/product/inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList();
}
