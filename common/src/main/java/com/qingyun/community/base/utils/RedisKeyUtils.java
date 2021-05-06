package com.qingyun.community.base.utils;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-05 09:21
 **/
public class RedisKeyUtils {
    private static final String SPLIT = ":";  // 分隔符
    private static final String PREFIX_KAPTCHA = "kaptcha";  // 验证码前缀


    public static  String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }
}
