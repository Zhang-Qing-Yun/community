package com.qingyun.community.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-28 19:30
 **/
@Component
@ConfigurationProperties("community.thread")
@Data
public class ThreadPoolProperties {
    //  核心线程数
    private int corePoolSize;
    //  最大线程数
    private int maxPoolSize;
    //  存活时间
    private long keepAliveTime;
}
