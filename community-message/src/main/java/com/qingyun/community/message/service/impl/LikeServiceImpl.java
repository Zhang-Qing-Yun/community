package com.qingyun.community.message.service.impl;

import com.qingyun.community.base.utils.RedisKeyUtils;
import com.qingyun.community.message.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @description： 使用redis来保存点赞业务，选用set数据结构，
 * key是like:entity:entityType:entityId，value值里的内容是对该entity点过赞的userId
 * @author: 張青云
 * @create: 2021-05-11 12:10
 **/
@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    // 实现了两个业务：1.给某个entity进行点赞；2.修改某个人收到的赞的个数
    // 需要保证事务性
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtils.getUserLikeKey(entityUserId);

                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();  // 开启事务
                if (isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();  // 提交事务
            }
        });
    }

    @Override
    public long getEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    @Override
    public List<Long> getEntitiesLikeCount(int entityType, List<Integer> entityIds) {
        List<Long> res = new ArrayList<>(entityIds.size());
        for(int entityId: entityIds) {
            res.add(getEntityLikeCount(entityType, entityId));
        }
        return res;
    }

    @Override
    public int getEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    @Override
    public List<Integer> getEntitiesLikeStatus(int userId, int entityType, List<Integer> entityIds) {
        List<Integer> res = new ArrayList<>(entityIds.size());
        for(int entityId: entityIds) {
            res.add(getEntityLikeStatus(userId, entityType, entityId));
        }
        return res;
    }

    @Override
    public int getUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtils.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
