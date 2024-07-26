package com.wx.YX.sys.controller;


import com.wx.YX.common.result.Result;
import com.wx.YX.model.sys.Region;
import com.wx.YX.sys.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 地区表 前端控制器
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */
@RestController
@RequestMapping("/admin/sys/region")
@CrossOrigin
@Api(tags = "区域管理接口")
public class RegionController {
    @Autowired
    private RegionService regionService;

    @ApiOperation("根据区域关键字查询区域列表信息")
    @GetMapping("/findRegionByKeyword/{keyword}")
    public Result findRegionByKeyword(@PathVariable String keyword){
        List<Region> list=regionService.getRegionByKeyword(keyword);
        return Result.ok(list);
    }


    @ApiOperation("根据上一级区域id查询区域信息")
    @GetMapping("/findByParentId/{parentId}")
    public Result findByParentId(@PathVariable Long parentId){

        List<Region> list=regionService.getRegionByParentId(parentId);
        return Result.ok(list);
    }

}

