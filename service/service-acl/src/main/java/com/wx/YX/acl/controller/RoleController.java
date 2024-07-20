package com.wx.YX.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.acl.service.RoleService;
import com.wx.YX.common.result.Result;
import com.wx.YX.model.acl.Role;
import com.wx.YX.vo.acl.RoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "角色接口")
@RestController
@RequestMapping("/admin/acl/role")
@CrossOrigin
public class RoleController {

    @Autowired
    private RoleService roleService;

    @ApiOperation("角色条件分页查询")
    @GetMapping("{current}/{limit}")
    public Result pageList(@ApiParam(name = "roleQueryVo", value = "查询对象", required = false) RoleQueryVo roleQueryVo,
                           @ApiParam(name = "current", value = "当前页码", required = true)
                           @PathVariable Long current,
                           @ApiParam(name = "limit", value = "每页记录数", required = true) @PathVariable Long limit
                           ){
        //创建page页，传递当前页和每页记录数
        Page<Role> pageParam=new Page<>(current,limit);
        //调用service方法实现条件分页查询，返回分页对象

        IPage<Role> pageModel=roleService.selectRolePage(pageParam,roleQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("根据id查询角色")
    @GetMapping("get{id}")
    public Result get(@PathVariable Long id){

        Role role=roleService.getById(id);
        return Result.ok(role);
    }

    @ApiOperation(value = "新增角色")
    @PostMapping("save")
    public Result save(@RequestBody Role role) {
        boolean is_success=roleService.save(role);
        if(is_success){
            return Result.ok(null);
        }else {
            return Result.fail(null);
        }

    }


    @ApiOperation(value = "修改角色")
    @PutMapping("update")
    public Result updateById(@RequestBody Role role) {
        boolean is_success= roleService.updateById(role);
        if(is_success){
            return Result.ok(null);
        }else {
            return Result.fail(null);
        }

    }

    @ApiOperation(value = "删除角色")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
       boolean is_success= roleService.removeById(id);
        if(is_success){
            return Result.ok(null);
        }else {
            return Result.fail(null);
        }
    }
//json数组[]对应java中List集合
    @ApiOperation(value = "根据id列表批量删除角色")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        boolean is_success=roleService.removeByIds(idList);
        if(is_success){
            return Result.ok(null);
        }else {
            return Result.fail(null);
        }
    }
}
