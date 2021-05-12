package com.qingyun.community.message.service;

import java.util.List;
import java.util.Map;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-12 13:53
 **/

public interface FollowService {
    /**
     * 用户关注某个entity
     * @param userId
     * @param entityType
     * @param entityId
     */
    void follow(int userId, int entityType, int entityId);

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    void unfollow(int userId, int entityType, int entityId);

    /**
     * 某人关注的实体的个数
     * @param userId
     * @param entityType
     * @return
     */
    long getFolloweeCount(int userId, int entityType);

    /**
     * 查询实体的粉丝数量
     * @param entityType
     * @param entityId
     * @return
     */
    long getFollowerCount(int entityType, int entityId);

    /**
     * 查询当前用户是否关注过该实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    boolean hasFollowed(int userId, int entityType, int entityId);

    /**
     * 分页查询某用户关注的人
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Map<String, Object>> getFollowees(int userId, int offset, int limit);

    /**
     * 分页查询某用户的粉丝
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Map<String, Object>> getFollowers(int userId, int offset, int limit);
}
