package com.qingyun.community.base.annotation;

import com.qingyun.community.base.utils.Constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description 自定义注解，标注在需要登录的方法上
 * @author: 張青云
 * @create: 2021-05-05 22:47
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    int value() default Constant.AUTHORITY_USER;  // 登录用户的权限，默认为普通用户
}
