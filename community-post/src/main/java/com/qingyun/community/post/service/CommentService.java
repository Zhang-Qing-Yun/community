package com.qingyun.community.post.service;

import com.qingyun.community.post.pojo.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyun.community.post.pojo.Page;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-08
 */
public interface CommentService extends IService<Comment> {
    /**
     * 根据回复的entity的id分页查询评论
     * @param current
     * @param entityType
     * @param entityId
     * @return
     */
    Map<String, Object> getCommentByEntityId(Integer current, Integer entityType, Integer entityId);

    /**
     * 发布一条评论
     * @param comment
     */
    void addComment(Comment comment);
}
