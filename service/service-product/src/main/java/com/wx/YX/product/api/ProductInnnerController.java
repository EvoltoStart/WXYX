package com.wx.YX.product.api;

import com.wx.YX.model.product.Category;
import com.wx.YX.model.product.SkuInfo;
import com.wx.YX.product.service.CategoryService;
import com.wx.YX.product.service.SkuInfoService;
import com.wx.YX.vo.product.SkuInfoVo;
import com.wx.YX.vo.product.SkuStockLockVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
//@CrossOrigin
public class ProductInnnerController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuInfoService skuInfoService;

    //根据分类id获取分类信息
    @GetMapping("inner/getCategory/{categoryId}")
    public Category getCategory(@PathVariable Long categoryId){

        Category category=categoryService.getById(categoryId);
        return category;
    }

    //根据skuid获取sku信息
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        return skuInfoService.getById(skuId);
    }
    //根据skuid获取sku信息列表
    @PostMapping("inner/findSkuInfoList")
    public List<SkuInfo> findSkuInfoList(@RequestBody List<Long> skuIdList){
      return   skuInfoService.findSkuInfoList(skuIdList);
    }

    @PostMapping("inner/findCategoryList")
    public List<Category> findCategoryList(@RequestBody List<Long> categoryIds){
        return categoryService.listByIds(categoryIds);
    }

    //根据关键字匹配sku列表
    @GetMapping("inner/findSkuInfoByKeyword/{keyword}")
    public List<SkuInfo> findSkuInfoByKeyword(@PathVariable String keyword){
        return skuInfoService.findSkuInfoByKeyword(keyword);
    }

    //获取所有分类
    @GetMapping("inner/findAllCategoryList")
    public List<Category> findAllCategoryList(){
       List<Category> categoryList= categoryService.list();
       return categoryList;
    }

    //获取新人专享商品
    @GetMapping("inner/findNewPersonSkuInfoList")
    public List<SkuInfo> findNewPersonSkuInfoList(){
        List<SkuInfo> list=skuInfoService.findNewPersonSkuInfoList();
        return list;
    }

    //根据skuid获取sku信息
    @GetMapping("inner/getSkuInfoVo/{skuId}")
    public SkuInfoVo getSkuInfoVo(@PathVariable Long skuId){


        return skuInfoService.getSkuInfoVo(skuId);
    }

    //验证和是锁定库存
    @ApiOperation(value = "锁定库存")
    @PostMapping("inner/checkAndLock/{orderNo}")
    public Boolean checkAndLock(@RequestBody List<SkuStockLockVo> skuStockLockVoList, @PathVariable String orderNo) {
        return skuInfoService.checkAndLock(skuStockLockVoList, orderNo);
    }

}
