package com.wx.YX.common.exception;

import com.wx.YX.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class yxException extends RuntimeException{
    private Integer code;//异常状态码

    public yxException(Integer code, String message) {

        super(message);
        this.code = code;
    }
    public yxException(ResultCodeEnum resultCodeEnum) {

        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

}
