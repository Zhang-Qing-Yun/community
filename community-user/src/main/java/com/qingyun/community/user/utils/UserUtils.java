package com.qingyun.community.user.utils;

import java.util.UUID;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-03 19:26
 **/
public class UserUtils {
    //random String
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
