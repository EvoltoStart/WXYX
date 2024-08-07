package com.wx.YX.common.auth;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

public class LoginMvcConfigurerAdapter extends WebMvcConfigurationSupport {
    @Resource
    private RedisTemplate redisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginInterceptor(redisTemplate)).addPathPatterns("/api/**").excludePathPatterns("/api/user/weixin/wxLogin/*");
        super.addInterceptors(registry);
    }
}
