package com.qingyun.community.post.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-06-01 20:25
 **/
@Configuration
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String redisAddress;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisAddress + ":" + redisPort);
        return Redisson.create(config);
    }
}
