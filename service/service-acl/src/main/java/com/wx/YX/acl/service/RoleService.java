package com.wx.YX.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.acl.Role;
import com.wx.YX.vo.acl.RoleQueryVo;

public interface RoleService extends IService<Role> {

    //角色查询（条件分页查询）

    IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo);
}
