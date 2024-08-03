package com.wx.YX.acl.controller;

import com.wx.YX.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Api(tags = "登录接口")
@RestController
@RequestMapping("/admin/acl/index")
//@CrossOrigin    //跨域
public class IndexController {

    /**
     * 1、请求登陆的login
     */
    @ApiOperation("登录")
    @PostMapping("/login")
    public Result login() {
        Map<String,Object> map = new HashMap<>();
        map.put("token","admin-token");
        return Result.ok(map);
    }

    /**
     * 2 获取用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result info(){
        Map<String,Object> map = new HashMap<>();
        map.put("name","wx");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        return Result.ok(map);
    }

    /**
     * 3 退出
     */
    @ApiOperation("退出")
    @PostMapping("/logout")
    public Result logout(){
        return Result.ok(null);
    }
}
