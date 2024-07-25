package com.wx.YX.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.acl.mapper.PermissionMapper;
import com.wx.YX.acl.service.PermissionService;
import com.wx.YX.acl.utils.PermissionHelper;
import com.wx.YX.model.acl.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 权限服务实现类
 * </p>
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission>
        implements PermissionService {
    @Autowired PermissionService permissionService;

    //获取所有菜单

    @Override
    public List<Permission> queryAllPermission() {
        //查询所有菜单
        List<Permission> allPermissionList=baseMapper.selectList(null);
        //转换要求数据格式
        List<Permission> result= PermissionHelper.buildPermission(allPermissionList);
        return result;
    }

    //    根据角色id获取菜单
    @Override
    public List<Permission> queryAllRolePermission(List<Long> permissionIdList) {
        //查询所有菜单
        List<Permission> allPermissionList=baseMapper.selectList(null);
        //转换要求数据格式
        List<Permission> result= PermissionHelper.buildRolePermission(allPermissionList,permissionIdList);
        return result;
    }


    //递归删除菜单
    @Override
    public void removeChildById(Long id) {
        List<Long>  idList=new ArrayList<>();
        this.getAllPermissionId(id,idList);
        idList.add(id);
        baseMapper.deleteBatchIds(idList);

    }

    @Override
    public List<Permission> getAllPermission(List<Long> list) {

        List<Permission>  PermissionList=  baseMapper.selectBatchIds(list);
        for (Permission permission : PermissionList) {
            permission.setSelect(true);
        }

        return PermissionHelper.buildPermission(PermissionList);
    }




    private void getAllPermissionId(Long id, List<Long> idList) {
        //根据当前id查询
        LambdaQueryWrapper<Permission> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPid,id);
        List<Permission> childList=baseMapper.selectList(wrapper);

        childList.stream().forEach(item->{
            idList.add(item.getId());
            this.getAllPermissionId(item.getId(),idList);
        });
    }


    /**
     *	递归获取子节点
     * @param id
     * @param idList
     */
//    private void selectChildListById(Long id, List<Long> idList) {
//        List<Permission> childList = baseMapper.selectList(new QueryWrapper<Permission>().eq("pid", id).select("id"));
//        childList.stream().forEach(item -> {
//            idList.add(item.getId());
//            this.selectChildListById(item.getId(), idList);
//        });
//    }
}
