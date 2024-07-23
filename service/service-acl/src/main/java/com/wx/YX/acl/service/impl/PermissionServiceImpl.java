package com.wx.YX.acl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.acl.mapper.PermissionMapper;
import com.wx.YX.acl.service.PermissionService;
import com.wx.YX.model.acl.Permission;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
