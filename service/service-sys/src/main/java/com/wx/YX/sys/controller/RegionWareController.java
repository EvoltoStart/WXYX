package com.wx.YX.sys.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.common.result.Result;
import com.wx.YX.model.sys.RegionWare;
import com.wx.YX.vo.sys.RegionWareQueryVo;
import com.wx.YX.sys.service.RegionWareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 城市仓库关联表 前端控制器
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */

@Api(tags = "开通区域接口")
@RestController
@RequestMapping("/admin/sys/regionWare")
@CrossOrigin
public class RegionWareController {
    @Autowired
    private RegionWareService regionWareService;

    //开通区域列表
    @ApiOperation("开通区域列表")
    @GetMapping("/{page}/{limit}")
    public Result list(@PathVariable long page, @PathVariable long limit, RegionWareQueryVo regionWareQueryVo) {
        Page<RegionWare> PageParm = new Page<>(page, limit);
        IPage<RegionWare> pageModel = regionWareService.selectPageRegionWare(PageParm, regionWareQueryVo);
        return Result.ok(pageModel);
    }

    //根据id查询开通区域
    @ApiOperation(value = "根据id查询开通区域")
    @GetMapping("/get/{id}")
    public Result getRegionWare(@PathVariable Long id){
         RegionWare regionWare = regionWareService.getById(id);
        return Result.ok(regionWare);
    }

    //添加开通区域
    @ApiOperation("添加开通区域")
    @PostMapping("/save")
    public Result addRegionWare(@RequestBody RegionWare regionWare) {
        regionWareService.saveRegionWare(regionWare);
        return Result.ok();
    }

    //删除开通区域
    @ApiOperation("删除开通区域")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable String id) {
        regionWareService.removeById(id);
        return Result.ok();
    }

    //批量删除开通区域
    @ApiOperation("value = '批量删除开通区域'")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        regionWareService.removeByIds(idList);
        return Result.ok();

    }

    //取消开通区域
    @ApiOperation("取消开通区域")
    @PostMapping("/updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        regionWareService.updateStatus(id, status);
        return Result.ok();

    }
}


