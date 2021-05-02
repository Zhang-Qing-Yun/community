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

    //  查询帖子并分页
    Map<String, Object> getPost(Integer current, Integer userId);

}
