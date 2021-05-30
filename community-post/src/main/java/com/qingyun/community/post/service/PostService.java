package com.qingyun.community.post.service;

import com.qingyun.community.post.pojo.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-02
 */
public interface PostService extends IService<Post> {

    /**
     * 查询帖子并分页
     * @param current
     * @param userId
     * @param orderMode 排序方式，如果0是则按type降序，时间降序；如果是1则按type降序，评分降序，时间降序来排
     * @return
     */
    Map<String, Object> getPost(Integer current, Integer userId, Integer orderMode);

    /**
     * 发帖
     * @param post
     */
    void postOne(Post post);

    /**
     * 获取某个帖子的详情
     * @param id
     * @return
     */
    Post getPostDetail(Integer id);

    /**
     * 更新帖子的数量
     * @param id 帖子id
     * @param newCount 帖子的新评论数
     */
    void updateCommentCount(Integer id, Integer newCount);

    /**
     * 修改帖子的类型
     * @param id 帖子id
     * @param type 要修改成的值
     * @return 修改后的帖子
     */
    Post updateType(Integer id, Integer type);

    /**
     * 修改帖子的状态
     * @param id 帖子id
     * @param status 要修改成的值
     * @return 修改后的帖子
     */
    Post updateStatus(Integer id, Integer status);

    /**
     * 修改帖子的分数
     * @param id
     * @param score
     */
    void updateScore(Integer id, Double score);
}
