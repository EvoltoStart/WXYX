package com.wx.YX.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wx.YX.user.mapper.LeaderMapper;
import com.wx.YX.user.mapper.UserDeliverMapper;
import com.wx.YX.user.mapper.UserMapper;
import com.wx.YX.model.user.Leader;
import com.wx.YX.model.user.User;
import com.wx.YX.model.user.UserDelivery;
import com.wx.YX.user.service.UserService;
import com.wx.YX.vo.user.LeaderAddressVo;
import com.wx.YX.vo.user.UserLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserDeliverMapper userDeliverMapper;
    @Autowired
    private LeaderMapper leaderMapper;

    @Override
    public User getByOpenid(String openId) {
        User user= baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        return user;
    }

    @Override
    public LeaderAddressVo getLeaderAddressVoByUserId(Long id) {
       UserDelivery userDelivery= userDeliverMapper.selectOne(new LambdaQueryWrapper<UserDelivery>().eq(UserDelivery::getUserId,id).eq(UserDelivery::getIsDefault,1));
       if(userDelivery==null){
           return null;
       }
       //拿到查询团长id查询leader表查询团长其他信息
       Leader leader=leaderMapper.selectById(userDelivery.getLeaderId());
        LeaderAddressVo leaderAddressVo = new LeaderAddressVo();
        BeanUtils.copyProperties(leader, leaderAddressVo);//用于在两个 Java 对象之间复制相同名称的属性,将leader相同复制leaderAddressVo
        leaderAddressVo.setUserId(id);
        leaderAddressVo.setLeaderId(leader.getId());
        leaderAddressVo.setLeaderName(leader.getName());
        leaderAddressVo.setLeaderPhone(leader.getPhone());
        leaderAddressVo.setWareId(userDelivery.getWareId());
        leaderAddressVo.setStorePath(leader.getStorePath());
        return leaderAddressVo;
    }

    @Override
    public UserLoginVo getUserLoginVo(Long id) {
        User user=baseMapper.selectById(id);
        UserLoginVo userLoginVo=new UserLoginVo();
        userLoginVo.setUserId(id);
        userLoginVo.setNickName(user.getNickName());
        userLoginVo.setPhotoUrl(user.getPhotoUrl());
        userLoginVo.setIsNew(user.getIsNew());
        userLoginVo.setOpenId(user.getOpenId());
        UserDelivery userDelivery= userDeliverMapper.selectOne(new LambdaQueryWrapper<UserDelivery>().eq(UserDelivery::getUserId,id).eq(UserDelivery::getIsDefault,1));
        if(userDelivery!=null){
            userLoginVo.setLeaderId(userDelivery.getLeaderId());
            userLoginVo.setWareId(userDelivery.getWareId());
        }else {
            userLoginVo.setLeaderId(1L);
            userLoginVo.setWareId(1L);
        }
        return userLoginVo;
    }
}
