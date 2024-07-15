package com.wx.YX.common.exception;

import com.wx.YX.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//AOP切面
@ControllerAdvice
public class GlobalExceeptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody //返回json数据
    public Result err(Exception e){
        e.printStackTrace();
        return Result.fail(null);
    }

    //自定义异常处理
    @ExceptionHandler(yxException.class)
    @ResponseBody
    public Result err(yxException e){
        return Result.fail(null);
    }
}
