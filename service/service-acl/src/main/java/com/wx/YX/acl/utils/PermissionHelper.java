package com.wx.YX.acl.utils;

import com.wx.YX.model.acl.Permission;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static  List<Permission> buildRolePermission(List<Permission> allList,List<Long> ids){
        List<Permission> trees=new ArrayList<>();
        for (Permission permissionNode : allList) {
            if( permissionNode.getPid().equals(0L)){
                permissionNode.setLevel(1);
                if(ids.contains(permissionNode.getId())) {
                    permissionNode.setSelect(true);
                }
                //调用方法，从第一层开始往下找
                trees.add(findRolePermissionChildren(permissionNode,allList,ids));
            }
        }
        return trees;

    }

    private static Permission findRolePermissionChildren(Permission permissionNode, List<Permission> allList,List<Long> ids) {
        permissionNode.setChildren(new ArrayList<>());
        for(Permission it: allList){
            if(permissionNode.getId().longValue()== it.getPid()){ //子节点pid==父节点id
                int level=permissionNode.getLevel()+1;
                it.setLevel(level);
                if(ids.contains(it.getId())) {
                    it.setSelect(true);
                }
                permissionNode.getChildren().add(findRolePermissionChildren(it,allList,ids));
            }
        }
        return permissionNode;
    }

    public static List<Permission> buildPermission(List<Permission> allList){
        //创建最终数据封装list集合
        List<Permission> trees=new ArrayList<>();

        for (Permission permissionNode : allList) {
            if( permissionNode.getPid().equals(0L)){
                permissionNode.setLevel(1);
                //调用方法，从第一层开始往下找
                trees.add(findChildren(permissionNode,allList));
            }
        }
        return trees;
    }

    //递归找子节点
    private static Permission findChildren(Permission permissionNode, List<Permission> allList) {
        permissionNode.setChildren(new ArrayList<>());
        for(Permission it: allList){
            if(permissionNode.getId().longValue()== it.getPid()){ //子节点pid==父节点id
                int level=permissionNode.getLevel()+1;
                it.setLevel(level);
                /***
                 *大雷啊，完全没必要
                 */

//                if(permissionNode.getChildren()==null){
//                    permissionNode.setChildren(new ArrayList<>());
//                }
                permissionNode.getChildren().add(findChildren(it,allList));
            }
        }
        return permissionNode;
    }
}
