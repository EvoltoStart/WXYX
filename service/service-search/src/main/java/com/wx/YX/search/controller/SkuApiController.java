package com.wx.YX.search.controller;

import com.wx.YX.common.result.Result;
import com.wx.YX.model.search.SkuEs;
import com.wx.YX.search.service.SkuService;
import com.wx.YX.vo.search.SkuEsQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    //查询分类商品
    @ApiOperation("查询分类商品")
    @GetMapping("{page}/{limit}}")
    public Result listSku(@PathVariable Integer page, @PathVariable Integer limit, SkuEsQueryVo skuEsQueryVo) {
        //0代表第一页
        Pageable pageable= PageRequest.of(page-1,limit);
        Page<SkuEs> page1Mode=skuService.search(pageable,skuEsQueryVo);
        return Result.ok(page1Mode);
    }

    //更新商品热度
    @GetMapping("inner/incrHotScore/{skuId}")
    public Boolean incrHotScore(@PathVariable("skuId") Long skuId) {
        skuService.incrHotScore(skuId);
        return true;
    }
}