package com.wx.YX.common.result;

import lombok.Data;

@Data
public class Result<T> {
    //状态码
    private Integer code;
    //信息
    private String message;
    //数据
    private T data;

    private Result() {

    }

    //设置数据的方法
    public static <T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        if (data != null) {
            result.setData(data);
            result.setCode(resultCodeEnum.getCode());
            result.setMessage(resultCodeEnum.getMessage());
        } else {
            result.setCode(500);
            result.setMessage("fail");
        }
        return result;
    }
    public static<T> Result<T> ok(T data){
        Result<T> result = build(data, ResultCodeEnum.SUCCESS);

        return result;
    }
    public static <T> Result<T> fail(T data){
        Result<T> result=build(data,ResultCodeEnum.FAIL);
        return  result;
    }
}
