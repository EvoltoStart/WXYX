package com.wx.YX.common.auth;

import com.wx.YX.common.constant.RedisConst;
import com.wx.YX.common.utils.helper.JwtHelper;
import com.wx.YX.model.user.User;
import com.wx.YX.vo.user.UserLoginVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLoginInterceptor implements HandlerInterceptor {
    private RedisTemplate redisTemplate;
    public UserLoginInterceptor(RedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;

    }
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        this.getUserLoginVo(request);
        return true;
    }

    private void getUserLoginVo(HttpServletRequest request) {
        //从请求头获取token

       String token= request.getHeader("token");
        //判断token不为空
        if (!StringUtils.isEmpty(token)) {

            Long userId= JwtHelper.getUserId(token);
            //根据用户id从redis获取用户数据
            UserLoginVo userLoginVo= (UserLoginVo) redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX+userId);
            //获取数据放到ThreadLocal
            if(userLoginVo!=null){
                AuthContextHolder.setUserId(userLoginVo.getUserId());
                AuthContextHolder.setWareId(userLoginVo.getWareId());
                AuthContextHolder.setUserLoginVo(userLoginVo);
            }
        }
    }
}
