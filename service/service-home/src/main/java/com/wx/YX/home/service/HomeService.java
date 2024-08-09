package com.wx.YX.home.service;

import java.util.Map;

public interface HomeService {
    //首页数据显示接口
    Map<String, Object> homeDate(Long userId);
}
