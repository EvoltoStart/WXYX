package com.wx.YX.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.wx.YX.common.auth.AuthContextHolder;
import com.wx.YX.common.constant.RedisConst;
import com.wx.YX.common.exception.yxException;
import com.wx.YX.common.result.Result;
import com.wx.YX.common.result.ResultCodeEnum;
import com.wx.YX.common.utils.helper.JwtHelper;
import com.wx.YX.enums.UserType;
import com.wx.YX.model.user.User;
import com.wx.YX.user.service.UserService;
import com.wx.YX.user.utils.ConstantPropertiesUtil;
import com.wx.YX.user.utils.HttpClientUtils;
import com.wx.YX.vo.user.LeaderAddressVo;
import com.wx.YX.vo.user.UserLoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Api(tags = "用户登录接口")
@RestController
@RequestMapping("/api/user/weixin")
public class WeixinApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "微信登录获取openid(小程序)")
    @GetMapping("/wxLogin/{code}")
    public Result loginWx(@PathVariable String code) {
        //获取授权临时票据
        System.out.println("微信授权服务器回调。。。。。。"+code);
        if (StringUtils.isEmpty(code)) {
            throw new yxException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }



        //拼接请求地址+参数
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/jscode2session")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&js_code=%s")
                .append("&grant_type=authorization_code");

        String wxOpenAppId=ConstantPropertiesUtil.WX_OPEN_APP_ID;
        String wxOpenAppSecret=ConstantPropertiesUtil.WX_OPEN_APP_SECRET;
        //使用code和appid（小程序id）以及appscrect(小程序密钥)换取access_token
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                wxOpenAppId,
                wxOpenAppSecret,
                code);

        String result = null;
        try {
            //HttpClient发送get请求
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new yxException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        //请求微信接口服务，返回俩个值，session_key,openid
        System.out.println("使用code换取的access_token结果 = " + result);
        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            throw new yxException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        String accessToken = resultJson.getString("session_key");
        String openId = resultJson.getString("openid");

        //TODO 为了测试，openId写固定
//        String accessToken = "";
//        String openId = "odo3j4uGJf6Hl2FopkEOLGxr7LE4";

        //根据access_token获取微信用户的基本信息
        //先根据openid进行数据库查询
        User user = userService.getByOpenid(openId);
        // 如果没有查到用户信息,那么调用微信个人信息获取的接口
        if(null == user){
            user = new User();
            user.setOpenId(openId);
            user.setNickName(openId);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            userService.save(user);
        }

        //根据userid查询
        //团长 leader表
        //提货点 user_delivery表
        LeaderAddressVo leaderAddressVo = userService.getLeaderAddressVoByUserId(user.getId());
        Map<String, Object> map = new HashMap<>();
        String name = user.getNickName();
        map.put("user", user);
        map.put("leaderAddressVo", leaderAddressVo);
        String token = JwtHelper.createToken(user.getId(), name);
        map.put("token", token);
//        if(user.getUserType() == UserType.LEADER) {
//            Leader leader = leaderService.getLeader();
//            map.put("leader", leader);
//        }
        UserLoginVo userLoginVo = this.userService.getUserLoginVo(user.getId());
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + user.getId(), userLoginVo, RedisConst.USERKEY_TIMEOUT, TimeUnit.DAYS);
        return Result.ok(map);
    }

//添加更新微信用户方法
    @PostMapping("/auth/updateUser")
    @ApiOperation(value = "更新用户昵称与头像")
    public Result updateUser(@RequestBody User user) {
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]", "*"));//表情替换
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok(null);
    }
}
