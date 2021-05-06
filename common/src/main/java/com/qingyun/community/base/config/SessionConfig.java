package com.qingyun.community.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-05 11:58
 **/
@Configuration
public class SessionConfig {
//    @Bean
//    public CookieSerializer cookieSerializer() {
//        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
//        //  设置cookie的作用域
//        //  TODO：注意这里的路径
//        cookieSerializer.setDomainName("localhost:88");
//
//        return cookieSerializer;
//    }


    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
