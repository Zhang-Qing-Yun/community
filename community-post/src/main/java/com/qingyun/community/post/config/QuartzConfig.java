package com.qingyun.community.post.config;

import com.qingyun.community.post.quartz.PostScoreRefreshJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-28 22:02
 **/
@Configuration
public class QuartzConfig {
    @Value("${flush-post-score-time}")
    private int FLUSH_SCORE_TIME;

    private static final String REFRESH_SCORE_IDENTITY = "postScoreRefreshJob";


    @Bean
    public JobDetail refreshScoreJobDetail() {
        // 链式编程,可以携带多个参数,在Job类中声明属性 + setter方法
        return JobBuilder.newJob(PostScoreRefreshJob.class)
                .withIdentity(REFRESH_SCORE_IDENTITY)
                .storeDurably().build();
    }

    @Bean
    public Trigger sampleJobTrigger(){
        //  TODO: 可以考虑使用定时任务来将热帖预热到缓存里
        //  TODO：可以考虑使用延时队列来将点赞数发生变化的持久化到数据库里
        // 每隔两秒执行一次
        SimpleScheduleBuilder scheduleBuilder =
                SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInMinutes(FLUSH_SCORE_TIME)  // 任务执行间隔
                        .repeatForever();
        return TriggerBuilder.newTrigger()
                .forJob(refreshScoreJobDetail())
                .withIdentity(REFRESH_SCORE_IDENTITY)
                .withSchedule(scheduleBuilder)
                .build();
    }
}
