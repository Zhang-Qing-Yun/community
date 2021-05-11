package com.qingyun.community.message.service;

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
     * 查询某人对某entity的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return 返回1说明点过赞，返回0说明没有点过赞
     */
    int getEntityLikeStatus(int userId, int entityType, int entityId);

    /**
     * 获取某个用户收到的赞的个数
     * @param userId
     * @return
     */
    int getUserLikeCount(int userId);
}
