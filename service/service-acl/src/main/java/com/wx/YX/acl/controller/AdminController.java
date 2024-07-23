package com.wx.YX.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wx.YX.acl.service.AdminService;
import com.wx.YX.acl.service.RoleService;
import com.wx.YX.common.result.Result;
import com.wx.YX.model.acl.Admin;
import com.wx.YX.vo.acl.AdminQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wx.YX.common.utils.MD5;

import java.lang.ref.PhantomReference;
import java.util.List;
import java.util.Map;

@Api(tags = "用户接口")
@RestController
@RequestMapping("/admin/acl/user")
@CrossOrigin
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private RoleService roleService;

 //为用户分配角色
    @ApiOperation(value = "为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestParam Long adminId,
                           @RequestParam Long[] roleId) {
        roleService.saveAdminRole(adminId,roleId);
        return Result.ok();

    }

    //获取所有角色
    @ApiOperation(value = "根据用户获取角色数据")
    @GetMapping("/toAssign/{adminId}")
    public Result toAssign(@PathVariable Long adminId) {
        Map<String, Object> roleMap = roleService.getRoleByAdminId(adminId);
        return Result.ok(roleMap);
    }

    @ApiOperation("用户列表")
    @GetMapping("{page}/{limit}")
    public Result list(@ApiParam(name = "page", value = "当前页码", required = true)
                       @PathVariable Long page,

                       @ApiParam(name = "limit", value = "每页记录数", required = true)
                       @PathVariable Long limit,

                       @ApiParam(name = "roleQueryVo", value = "查询对象", required = false)
                       AdminQueryVo adminQueryVo){
        Page<Admin> pageParam=new Page<>(page,limit);
        IPage<Admin> pageModel=adminService.selectPage(pageParam,adminQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        Admin admin=adminService.getById(id);
        return Result.ok(admin);
    }
    @ApiOperation("添加用户")
    @PostMapping("save")
    public Result save(@RequestBody Admin admin){
        admin.setPassword(MD5.encrypt(admin.getPassword()));
        boolean save=adminService.save(admin);
        if(save){
            return Result.ok();
        }else return Result.fail(null);
    }
    @ApiOperation("修改用户")
    @PutMapping("update")
    public Result update(@RequestBody Admin admin){

        boolean update=adminService.updateById(admin);
        return Result.ok();
    }
    @ApiOperation("根据id删除用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){

        boolean remove=adminService.removeById(id);
        if(remove){
            return Result.ok();
        }else return Result.fail(null);
    }
    @ApiOperation("批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){

        boolean remove=adminService.removeByIds(idList);
        if(remove){
            return Result.ok();
        }else return Result.fail(null);
    }

}

