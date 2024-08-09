package com.wx.YX.home.controller;

import com.wx.YX.common.auth.AuthContextHolder;
import com.wx.YX.common.result.Result;
import com.wx.YX.home.service.HomeService;
import com.wx.YX.vo.search.SkuEsQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class HomeApiController {
    @Api(tags = "首页接口")
    @RestController
    @RequestMapping("/api/home")
    public class HomeServiceImpl {

        @Autowired
        private HomeService homeService;

        @ApiOperation("首页数据显示接口")
        @GetMapping("index")
        public Result index(HttpServletRequest request) {
            Long userId = AuthContextHolder.getUserId();
            Map<String,Object> map= homeService.homeDate(userId);
            return Result.ok(map);
        }
    }


}
