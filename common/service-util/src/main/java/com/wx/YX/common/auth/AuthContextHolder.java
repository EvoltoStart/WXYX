package com.wx.YX.common.auth;

import com.wx.YX.vo.acl.AdminLoginVo;
import com.wx.YX.vo.user.UserLoginVo;


/**
 * 获取登录用户信息类
 * ThreadLocal工具类
 *
 */

public class AuthContextHolder {
    //用户id
    private static ThreadLocal<Long> userId = new ThreadLocal<Long>();
    //仓库id
    private static ThreadLocal<Long> wareId = new ThreadLocal<>();
    //会员基本信息
    private static ThreadLocal<UserLoginVo> userLoginVo = new ThreadLocal<>();

    //后台管理用户id
    private static ThreadLocal<Long> adminId = new ThreadLocal<Long>();
    //管理员基本信息
    private static ThreadLocal<AdminLoginVo> adminLoginVo = new ThreadLocal<>();

    public static Long getUserId(){
        return 31L;
    }

    public static void setUserId(Long _userId){
        userId.set(_userId);
    }

    public static Long getWareId(){
        return wareId.get();
    }

    public static void setWareId(Long _wareId){
        wareId.set(_wareId);
    }

    public static UserLoginVo getUserLoginVo() {
        return userLoginVo.get();
    }

    public static void setUserLoginVo(UserLoginVo _userLoginVo) {
        userLoginVo.set(_userLoginVo);
    }

    public static Long getAdminId() {
        return adminId.get();
    }

    public static void setAdminId(Long _adminId) {
        adminId.set(_adminId);
    }

    public static AdminLoginVo getAdminLoginVo() {
        return adminLoginVo.get();
    }

    public static void setAdminLoginVo(AdminLoginVo _adminLoginVo) {
        adminLoginVo.set(_adminLoginVo);
    }
}
