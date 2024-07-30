package com.wx.YX.search.service.impl;

import com.wx.YX.client.product.ProductFeignClient;
import com.wx.YX.enums.SkuType;
import com.wx.YX.model.product.Category;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.model.search.SkuEs;
import com.wx.YX.search.repository.SkureRepository;
import com.wx.YX.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private  SkureRepository skureRepository;
    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public void upperSku(Long skuId) {
        //通过远程调用，根据skuid获取相关数据
        SkuInfo skuInfo=productFeignClient.getSkuInfo(skuId);
        if(skuInfo==null){
            return;
        }
        Category category=productFeignClient.getCategory(skuInfo.getCategoryId());

        //获取数据封装skuEs对象
        SkuEs skuEs=new SkuEs();
        //封装分类
        if(category!=null){
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        //封装skuInfo信息部分
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if(Objects.equals(skuInfo.getSkuType(), SkuType.COMMON.getCode())) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        } else {
            //TODO 待完善-秒杀商品

        }
        skureRepository.save(skuEs);
    }

    @Override
    public void lowerSku(Long skuId) {
        skureRepository.deleteById(skuId);

    }
}
