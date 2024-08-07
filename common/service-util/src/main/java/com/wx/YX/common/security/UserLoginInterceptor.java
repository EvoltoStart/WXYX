package com.wx.YX.common.security;

import com.wx.YX.common.auth.AuthContextHolder;
import com.wx.YX.common.constant.RedisConst;
import com.wx.YX.common.utils.helper.JwtHelper;
import com.wx.YX.vo.user.UserLoginVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLoginInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public UserLoginInterceptor(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        this.initUserLoginVo(request);
        return true;
    }

    private void initUserLoginVo(HttpServletRequest request){
        //从请求头获取token
        String token = request.getHeader("token");
        System.out.println(token);
        if (!StringUtils.isEmpty(token)) {
            Long userId = JwtHelper.getUserId(token);
            UserLoginVo userLoginVo = (UserLoginVo)redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX + userId);
            if(userLoginVo != null) {
                //将UserInfo放入上下文中
                AuthContextHolder.setUserId(userLoginVo.getUserId());
                AuthContextHolder.setWareId(userLoginVo.getWareId());
            }
        }
    }
}