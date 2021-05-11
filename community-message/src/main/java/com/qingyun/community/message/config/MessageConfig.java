package com.qingyun.community.message.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-09 19:24
 **/
@Configuration
@MapperScan("com.qingyun.community.message.mapper")
public class MessageConfig {
}
