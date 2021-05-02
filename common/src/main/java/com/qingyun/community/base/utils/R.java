package com.qingyun.community.base.utils;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-02 19:12
 **/
@Data
public class R {
    @ApiModelProperty(value = "是否成功")  // Swagger的注解
    private Boolean success;

    @ApiModelProperty(value = "返回码")
    private Integer code;

    @ApiModelProperty(value = "返回消息")
    private String message;

    @ApiModelProperty(value = "返回数据")
    private Map<String, Object> data = new HashMap<String, Object>();

    private R(){}

    public static R ok(){
        R r = new R();
        r.setSuccess(true);
        r.setCode(Code.Success.getCode());
        r.setMessage(Code.Success.getMessage());
        return r;
    }

    public static R error(){
        R r = new R();
        r.setSuccess(false);
        r.setCode(Code.Error.getCode());
        r.setMessage(Code.Error.getMessage());
        return r;
    }

    public R success(Boolean success){
        this.setSuccess(success);
        return this;
    }

    public R message(String message){
        this.setMessage(message);
        return this;
    }

    public R code(Integer code){
        this.setCode(code);
        return this;
    }

    public R data(String key, Object value){
        this.data.put(key, value);
        return this;
    }

    public R data(Map<String, Object> map){
        this.setData(map);
        return this;
    }
}
