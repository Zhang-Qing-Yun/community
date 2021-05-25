package com.qingyun.community.message.service;

import java.util.List;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-11 12:10
 **/
public interface LikeService {
    /**
     * 给某个entity点赞
     * @param userId 点赞人的id
     * @param entityType 点赞的entity的类型
     * @param entityId 点赞的entity的id
     */
    void like(int userId, int entityType, int entityId, int entityUserId);

    /**
     * 查询某个entity的点赞数
     * @param entityType
     * @param entityId
     * @return
     */
    long getEntityLikeCount(int entityType, int entityId);

    /**
     * 批量获取某些entity的点赞数
     * @param entityType
     * @param entityIds
     * @return
     */
    List<Long> getEntitiesLikeCount(int entityType, List<Integer> entityIds);

    /**
     * 查询某人对某entity的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return 返回1说明点过赞，返回0说明没有点过赞
     */
    int getEntityLikeStatus(int userId, int entityType, int entityId);

    /**
     * 批量获取某人对某些entity的点赞状态
     * @param userId
     * @param entityType
     * @param entityIds
     * @return
     */
    List<Integer> getEntitiesLikeStatus(int userId, int entityType, List<Integer> entityIds);

    /**
     * 获取某个用户收到的赞的个数
     * @param userId
     * @return
     */
    int getUserLikeCount(int userId);
}
