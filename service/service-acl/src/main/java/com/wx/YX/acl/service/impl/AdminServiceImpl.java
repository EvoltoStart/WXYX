package com.wx.YX.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.acl.mapper.AdminMapper;
import com.wx.YX.acl.service.AdminService;
import com.wx.YX.model.acl.Admin;
import com.wx.YX.vo.acl.AdminQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Override
    public IPage<Admin> selectPage(Page<Admin> pageParam, AdminQueryVo userQueryVo) {
        String username= userQueryVo.getUsername();
        String name=userQueryVo.getName();
        LambdaQueryWrapper<Admin> wrapper=new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(username)){
            wrapper.eq(Admin::getUsername,username);
        }
        if(!StringUtils.isEmpty(name)){
            wrapper.like(Admin::getName,name);
        }
        return baseMapper.selectPage(pageParam,wrapper);
    }
}
