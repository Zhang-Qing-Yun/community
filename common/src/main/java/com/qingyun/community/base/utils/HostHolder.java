package com.qingyun.community.base.utils;

import com.qingyun.community.base.pojo.User;
import org.springframework.stereotype.Component;

/**
 * @description ThreadLocal的使用工具类
 * @author: 張青云
 * @create: 2021-05-05 22:54
 **/
@Component
public class HostHolder {

    //  多个线程共用一个ThreadLocal对象不会有问题
    private ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public void  set(User user){
        threadLocal.set(user);
    }

    public User get(){
        return threadLocal.get();

    }

    public void remove(){
        threadLocal.remove();
    }
}
