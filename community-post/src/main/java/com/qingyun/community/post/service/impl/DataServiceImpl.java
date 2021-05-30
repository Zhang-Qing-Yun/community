package com.qingyun.community.post.service.impl;

import com.qingyun.community.base.utils.RedisKeyUtils;
import com.qingyun.community.post.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-28 09:57
 **/
@Service
public class DataServiceImpl implements DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void addUV(String ip) {
        String uvKey = RedisKeyUtils.getUVKey(formatter.format(LocalDateTime.now()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    @Override
    public long calculateUV(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("待统计的日期参数不合法！");
        }
        //  合并后的数据的新key
        String unionKey = RedisKeyUtils.getUVKey(formatter.format(start), formatter.format(end));
        //  收集要统计的key
        List<String> list = new LinkedList<>();
        while (!start.isAfter(end)) {
            list.add(RedisKeyUtils.getUVKey(formatter.format(start)));
            start = start.plusDays(1);
        }
        return redisTemplate.opsForHyperLogLog().union(unionKey, list.toArray());
    }

    @Override
    public void recordDAU(int userId) {
        String dauKey = RedisKeyUtils.getDAUKey(formatter.format(LocalDateTime.now()));
        redisTemplate.opsForValue().setBit(dauKey, userId, true);
    }

    @Override
    public long calculateDAU(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("待统计的日期参数不合法！");
        }
        String unionKey = RedisKeyUtils.getDAUKey(formatter.format(start), formatter.format(end));
        //  收集要统计的key，这里需要把key转换成字节数组
        List<byte[]> list = new LinkedList<>();
        while (!start.isAfter(end)) {
            String key = RedisKeyUtils.getDAUKey(formatter.format(start));
            list.add(key.getBytes());
            start = start.plusDays(1);
        }
        // 进行OR运算
        return (long) redisTemplate.execute((RedisCallback) connection -> {
            connection.bitOp(RedisStringCommands.BitOperation.OR,
                    unionKey.getBytes(), list.toArray(new byte[0][0]));
            return connection.bitCount(unionKey.getBytes());
        });
    }


}
