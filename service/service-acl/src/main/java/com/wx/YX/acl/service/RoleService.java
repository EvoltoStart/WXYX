package com.wx.YX.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.acl.Role;
import com.wx.YX.vo.acl.RoleQueryVo;

import java.util.Map;

public interface RoleService extends IService<Role> {

    //角色查询（条件分页查询）

    IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo);

    /**
     * 分配角色
     *
     * @param adminId
     * @param roleId
     */
    void saveAdminRole(Long adminId, Long[] roleId);

    /**
     * 根据用户获取角色数据
     *
     * @param adminId
     * @return
     */


    Map<String, Object> getRoleByAdminId(Long adminId);

    
}
