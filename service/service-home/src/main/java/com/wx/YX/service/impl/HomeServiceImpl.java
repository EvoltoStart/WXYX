package com.wx.YX.service.impl;

import com.wx.YX.client.product.ProductFeignClient;
import com.wx.YX.client.search.SkuFeignClient;
import com.wx.YX.client.user.UserFeignClient;
import com.wx.YX.model.product.Category;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.model.search.SkuEs;
import com.wx.YX.service.HomeService;
import com.wx.YX.vo.user.LeaderAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private SkuFeignClient skuFeignClient;
    @Override
    public Map<String, Object> homeDate(Long userId) {
        //根据userid获取当前用户登录提货地址信息
        Map<String,Object> map=new HashMap<>();

        //远程调用service-user获取数据
        LeaderAddressVo leaderAddressVo=userFeignClient.getUserAddressByUserId(userId);
        map.put("leaderAddressVo",leaderAddressVo);

        //远程调用seervice-product获取所有分类
        List<Category> categoryList=productFeignClient.findAllCategoryList();
        map.put("categoryList",categoryList);

        //远程调用service-product获取新人专享商品
        List<SkuInfo> personSkuInfoList=productFeignClient.findNewPersonSkuInfoList();
        map.put("newPersonSkuInfoList",personSkuInfoList);
        //远程调用servicec-search获取爆款商品(通过es查询)---score字段评分降序排序
        List<SkuEs> skuEsList=skuFeignClient.findHotSkuList();
        map.put("hotSkuList",skuEsList);

        //封装获取数据到map。返回
        return map;
    }
}
