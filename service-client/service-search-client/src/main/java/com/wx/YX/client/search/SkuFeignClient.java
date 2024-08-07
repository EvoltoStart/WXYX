package com.wx.YX.client.search;

import com.wx.YX.model.search.SkuEs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("service-search")
public interface SkuFeignClient {
    //获取爆款商品
    @GetMapping("/api/search/sku/inner/findHotSkuList")
    public List<SkuEs> findHotSkuList();
}
