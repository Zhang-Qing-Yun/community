package com.qingyun.community.search.service;

import com.qingyun.community.search.pojo.Page;
import com.qingyun.community.search.pojo.Post;

import java.io.IOException;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-22 14:02
 **/
public interface ElasticsearchService {
    /**
     * 异步的向ES添加一条帖子文档
     * @param post
     */
    void addPostToES(Post post);

    /**
     * 从ES中删除对应id的文档
     * @param id
     */
    void deletePostFromES(int id);

    /**
     * 从ES里查询数据并分页
     * @param keyword
     * @param current
     * @param pageSize
     * @return
     */
    Page<Post> searchFromES(String keyword, Integer current, Integer pageSize) throws IOException;
}
