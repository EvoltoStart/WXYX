package com.wx.YX.common.utils.helper;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * **（1）JWT工具概述**
 *
 * JWT（Json Web Token）是为了在网络应用环境间传递声明而执行的一种基于JSON的开放标准。
 *
 * JWT的声明一般被用来在身份提供者和服务提供者间传递被认证的用户身份信息，以便于从资源服务器获取资源。比如用在用户登录上
 *
 * JWT最重要的作用就是对 token信息的**防伪**作用。
 *
 *
 *
 * **（2）JWT的原理**
 *
 * 一个JWT由**三个部分组成：公共部分、私有部分、签名部分**。最后由这三者组合进行base64编码得到JWT。
 *
 * ![img](images\0.00022527543306422325.png)
 *
 * 1、 公共部分
 *
 * 主要是该JWT的相关配置参数，比如签名的加密算法、格式类型、过期时间等等。
 *
 * Key=ATGUIGU
 *
 * 2、 私有部分
 *
 * 用户自定义的内容，根据实际需要真正要封装的信息。
 *
 * userInfo{用户的Id，用户的昵称nickName}
 *
 * 3、 签名部分
 *
 * SaltiP: 当前服务器的Ip地址!{linux 中配置代理服务器的ip}
 *
 * 主要用户对JWT生成字符串的时候，进行加密{盐值}
 *
 * 最终组成 key+salt+userInfo è token!
 *
 * base64编码，并不是加密，只是把明文信息变成了不可见的字符串。但是其实只要用一些工具就可以把base64编码解成明文，所以不要在JWT中放入涉及私密的信息。
 */
public class JwtHelper {

    private static long tokenExpiration = 365*24*60*60*1000;//过期时间
    private static String tokenSignKey = "ssyx";//大于4个字符！！！

    //根据userid+username生成token字符串
    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("yx-USER") //分组
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)) //设置过期时间
                .claim("userId", userId)     //
                .claim("userName", userName)  // jwt私有部分
                .signWith(SignatureAlgorithm.HS512, tokenSignKey) //加密
                .compressWith(CompressionCodecs.GZIP)  //压缩
                .compact();
        return token;
    }

    public static Long getUserId(String token) {
        if(StringUtils.isEmpty(token)) return null;

        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
        // return 1L;
    }

    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) return "";

        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userName");
    }

    public static void removeToken(String token) {
        //jwttoken无需删除，客户端扔掉即可。
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(7L, "admin");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUserName(token));
    }
}
