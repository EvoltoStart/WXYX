package com.wx.YX.search.controller;

import com.wx.YX.common.result.Result;
import com.wx.YX.model.search.SkuEs;
import com.wx.YX.search.service.SkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 商品搜索列表接口
 * </p>
 */
@Api(tags = "商品搜索")
@RestController
@RequestMapping("api/search/sku")
public class SkuApiController {

    @Autowired
    private SkuService skuService;

    @ApiOperation(value = "上架商品")
    @GetMapping("inner/upperSku/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId) {
        skuService.upperSku(skuId);
        return Result.ok();
    }

    @ApiOperation(value = "下架商品")
    @GetMapping("inner/lowerSku/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId) {
        skuService.lowerSku(skuId);
        return Result.ok();
    }
    @ApiOperation(value = "获取爆款商品")
    //获取爆款商品
    @GetMapping("inner/findHotSkuList")
    public List<SkuEs> findHotSkuList(){
        return skuService.findHotSkuList();
    }
}