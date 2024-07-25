package com.wx.YX.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.acl.mapper.RolePermissionMapper;
import com.wx.YX.acl.service.RolePermissionService;
import com.wx.YX.model.acl.Permission;
import com.wx.YX.model.acl.RolePermission;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;



@Repository
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
    @Override
    public List<Long> getRolePermissionId(Long roleId) {
        LambdaQueryWrapper<RolePermission>wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId,roleId);
        List<RolePermission> rolePermissionList=baseMapper.selectList(wrapper);


        return rolePermissionList.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
    }

    /***
     * 根据角色id删除角色权限,重新赋权
     * @param roleId
     * @param permissionId
     */
    @Override
    public void savePermission(Long roleId, Long[] permissionId) {
        for (Long id : permissionId) {

            RolePermission rolePermission=new RolePermission();

            rolePermission.setRoleId(roleId);

            rolePermission.setPermissionId(id);
            baseMapper.insert(rolePermission);
        }

    }

    @Override
    public void selectRolePermission(List<Permission> permissionsList, List<Long> permissionIdList) {


        for (Permission permission : permissionsList) {
            if(permissionIdList.contains(permission.getId())){
                permission.setSelect(true);
            }
        }
    }
}
