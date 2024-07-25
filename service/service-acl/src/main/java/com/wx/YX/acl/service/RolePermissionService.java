package com.wx.YX.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.acl.Permission;
import com.wx.YX.model.acl.RolePermission;

import java.util.List;

public interface RolePermissionService extends IService<RolePermission> {
    List<Long> getRolePermissionId(Long roleId);

    void savePermission(Long roleId, Long[] permissionId);

    void selectRolePermission(List<Permission> permissionsList, List<Long> permissionIdList);
}
