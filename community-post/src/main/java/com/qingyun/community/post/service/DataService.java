package com.qingyun.community.post.service;

import java.time.LocalDateTime;

/**
 * @description：统计网站的访问量(UV)和活跃用户(DAU)
 * UV统计使用ip来统计，一天之内一个ip至多会被统计一次，不论访问了多少次或多少个请求
 * @author: 張青云
 * @create: 2021-05-28 09:57
 **/
public interface DataService {
    /**
     * 向当前日期的访问量UV里增加一条数据
     * @param ip 用户的IP地址
     */
    void addUV(String ip);

    /**
     * 统计指定时间范围内的访问量UV数
     * @param start 开始时间
     * @param end 结束时间
     * @return 指定时间内的访问量UV
     */
    long calculateUV(LocalDateTime start, LocalDateTime end);

    /**
     * 将userId添加到当前日期的活跃用户中去
     * @param userId
     */
    void recordDAU(int userId);

    /**
     * 统计指定时间日期内的活跃用户
     * @param start
     * @param end
     * @return
     */
    long calculateDAU(LocalDateTime start, LocalDateTime end);
}
