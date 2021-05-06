package com.qingyun.community.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-02 18:22
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.qingyun"})  // 去扫描该包下的注解
@EnableDiscoveryClient  // 注册到nacos中
@EnableFeignClients  // 远程调用端
@EnableRedisHttpSession
public class PostApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostApplication.class, args);
    }
}
