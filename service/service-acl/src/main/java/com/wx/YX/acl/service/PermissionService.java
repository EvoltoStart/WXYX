package com.wx.YX.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.acl.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {
    List<Permission> queryAllPermission();

    void removeChildById(Long id);

    List<Permission> getAllPermission(List<Long> list);

    List<Permission> queryAllRolePermission(List<Long> permissionIdList);
}
