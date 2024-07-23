package com.wx.YX.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.acl.mapper.RoleMapper;
import com.wx.YX.acl.service.AdminRoleService;
import com.wx.YX.acl.service.RoleService;
import com.wx.YX.model.acl.AdminRole;
import com.wx.YX.model.acl.Role;
import com.wx.YX.vo.acl.RoleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired AdminRoleService adminRoleService;


    //角色查询（条件分页查询）
    @Transactional
    @Override
    public IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        //获取条件值
        String roleName=roleQueryVo.getRoleName();
        //创建mp条件对象
        LambdaQueryWrapper<Role> wrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(roleName)){
            wrapper.like(Role::getRoleName,roleName);
        }

        //调用mp分页查询方法
        Page<Role> rolePage=baseMapper.selectPage(pageParam,wrapper);

        return rolePage;
    }

    @Override
    public void saveAdminRole(Long adminId, Long[] roleIds) {


        //根据用户id先删除原来分配的角色
        LambdaQueryWrapper<AdminRole> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId,adminId);
        adminRoleService.remove(wrapper);


        //重新分配角色
//        for(Long reoleId:roleIds){
//            AdminRole adminRole=new AdminRole();
//            adminRole.setAdminId(adminId);
//            adminRole.setRoleId(reoleId);
//            adminRoleService.save(adminRole);
//        }
        List<AdminRole> list=new ArrayList<>();
        for(Long roleId:roleIds){

            AdminRole adminRole=new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(roleId);

            list.add(adminRole);
        }

        adminRoleService.saveBatch(list);
    }


    @Override
    public Map<String, Object> getRoleByAdminId(Long adminId) {


        List<Role> allRoleList=baseMapper.selectList(null);

        LambdaQueryWrapper<AdminRole> wrapper=new LambdaQueryWrapper<>();
        //设置查询条件，根据用户id adminid
        wrapper.eq(AdminRole::getAdminId,adminId);
        List<AdminRole> adminRoleList=adminRoleService.list(wrapper);
        
        //获取用户角色id的集合
        List<Long> roleIdList= adminRoleList.stream().
                map(item->item.getRoleId()).
                collect(Collectors.toList());

        List<Role> assignRoleList=new ArrayList<>();
        for(Role role:allRoleList){
            if(roleIdList.contains(role.getId())){
                assignRoleList.add(role);
            }
        }

        Map<String,Object> result=new HashMap<>();
        //所有角色
        result.put("allRolesList",allRoleList);
        //用户分配角色列表
        result.put("assignRoles",assignRoleList);
        return result;
    }
}
