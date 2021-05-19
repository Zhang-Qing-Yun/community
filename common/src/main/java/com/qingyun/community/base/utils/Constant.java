package com.qingyun.community.base.utils;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-08 10:11
 **/
public interface Constant {
    //实体类型：帖子
    int ENTITY_TYPE_POST= 1;
    //实体类型：评论
    int ENTITY_TYPE_COMMENT = 2;
    //实体类型：用户
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题: 评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题: 关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 系统用户id
     */
    int SYSTEM_USER_ID = 1;
}
