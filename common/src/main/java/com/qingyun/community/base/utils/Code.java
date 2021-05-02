package com.qingyun.community.base.utils;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-02 19:08
 **/
public enum Code {

    Error(20001, "出现错误"),
    Success(20000, "执行正常");

    private int code;
    private String message;

    Code(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
