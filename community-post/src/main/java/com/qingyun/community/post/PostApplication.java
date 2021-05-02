package com.qingyun.community.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-02 18:22
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.qingyun"})  // 去扫描该包下的注解
public class PostApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostApplication.class, args);
    }
}
