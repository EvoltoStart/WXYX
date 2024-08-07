package com.wx.YX.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wx.YX.model.user.User;
import com.wx.YX.vo.user.LeaderAddressVo;
import com.wx.YX.vo.user.UserLoginVo;

public interface UserService extends IService<User> {
    User getByOpenid(String openId);

    LeaderAddressVo getLeaderAddressVoByUserId(Long id);

    UserLoginVo getUserLoginVo(Long id);
}
