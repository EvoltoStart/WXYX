package com.wx.YX.sys.controller;


import com.wx.YX.common.result.Result;
import com.wx.YX.model.sys.Ware;
import com.wx.YX.sys.service.WareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 仓库表 前端控制器
 * </p>
 *
 * @author meng
 * @since 2024-07-26
 */
@RestController
@RequestMapping("/admin/sys/ware")
//@CrossOrigin
@Api(tags = "仓库管理接口")
public class WareController {
    @Autowired
    private WareService wareService;

    @ApiOperation("查询所有仓库")
    @GetMapping("findAllList")
    public Result findAllWareList(){
        List<Ware> list= wareService.list();
        return Result.ok(list);
    }

    //根据id查询仓库

    @ApiOperation("根据id查询仓库")
    @GetMapping("/get/{id}")
    public Result getWare(@PathVariable Long id){
         Ware ware = wareService.getById(id);
        return Result.ok(ware);
    }

    //添加仓库

    @ApiOperation("添加仓库")
    @PostMapping("save")
    public Result save(@RequestBody Ware ware){
         wareService.saveWare(ware);
        return Result.ok();
    }

    //修改仓库
    @ApiOperation("修改仓库")
    @PutMapping("update")
    public Result update(@RequestBody Ware ware){
         wareService.updateById(ware);
        return Result.ok();
    }

    //删除仓库
    @ApiOperation("删除仓库")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
         wareService.removeById(id);
        return Result.ok();
    }

    //批量删除
    @ApiOperation("批量删除仓库")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> isList){
         wareService.removeByIds(isList);
        return Result.ok();
    }
}

