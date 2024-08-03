package com.wx.YX.acl.controller;

import com.wx.YX.acl.service.PermissionService;
import com.wx.YX.acl.service.RolePermissionService;
import com.wx.YX.common.result.Result;
import com.wx.YX.model.acl.Permission;
import com.wx.YX.model.acl.RolePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "菜单接口")
@RestController
@RequestMapping("/admin/acl/permission")
//@CrossOrigin
public class PermissionController {
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RolePermissionService rolePermissionService;

    @ApiOperation("查询所有菜单")
    @GetMapping
    public Result list(){
        List<Permission> list=permissionService.queryAllPermission();
        return Result.ok(list);
    }

    @ApiOperation("添加菜单")
    @PostMapping("save")
    public Result save(@RequestBody Permission permission){
        permissionService.save(permission);
        return Result.ok();
    }

    @ApiOperation("修改菜单")
    @PostMapping("update")
    public Result update(@RequestBody Permission permission){
        permissionService.updateById(permission);
        return Result.ok();

    }

    @ApiOperation("删除菜单")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        permissionService.removeChildById(id);
        return  Result.ok();
    }
    @ApiOperation("查询某个角色的菜单")
    @GetMapping("/toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId) {
        List<Long> permissionIdList=rolePermissionService.getRolePermissionId(roleId);
       List<Permission> permissionsList=permissionService.queryAllRolePermission(permissionIdList);

       //rolePermissionService.selectRolePermission(permissionsList,permissionIdList);
        return Result.ok(permissionsList);
    }
    @ApiOperation("给角色分配权限")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long roleId,@RequestParam Long[] permissionId){
        List<Long> permissionIdList=rolePermissionService.getRolePermissionId(roleId);
        for(Long permissionIdd:permissionIdList){
            System.out.println(permissionIdd);
        }
        rolePermissionService.removeByIds(permissionIdList);
        rolePermissionService.savePermission(roleId,permissionId);
        return Result.ok();
    }

}
