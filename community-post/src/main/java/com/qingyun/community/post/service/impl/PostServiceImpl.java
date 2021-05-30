package com.qingyun.community.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyun.community.base.component.SensitiveFilter;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.mapper.PostMapper;
import com.qingyun.community.post.service.PostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-02
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    @Value("${page.size}")
    private int PAGE_SIZE;

    @Autowired
    private SensitiveFilter sensitiveFilter;



    @Override
    public Map<String, Object> getPost(Integer current, Integer userId, Integer orderMode) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        if(current == null || current <= 1) {
            current = 1;
        }
        Page<Post> page = new Page<>(current, PAGE_SIZE);
        Map<String, Object> map = new HashMap<>();

        if(userId != null) {
            wrapper.eq("user_id", userId);
        }
        //  查询没有被删除的
        wrapper.ne("status", 2);
        //  按type降序
        wrapper.orderByDesc("type");
        //  是否按评分降序
        if (orderMode == 1) {
            wrapper.orderByDesc("score");
        }
        //  按帖子id降序，这样就保证了时间顺序
        wrapper.orderByDesc("id");
        baseMapper.selectPage(page, wrapper);
        //  该页的记录
        List<Post> items = page.getRecords();
        //  总页数
        long pages = page.getPages();
        //  每页记录数
        long size = page.getSize();
        //  总记录数
        long total = page.getTotal();
        //  是否有下一页
        boolean hasNext = page.hasNext();
        //  是否有上一页
        boolean hasPrevious = page.hasPrevious();


        map.put("items", items);
        map.put("current", current);
        map.put("pages", pages);
        map.put("pageSize", size);
        map.put("total", total);
        map.put("hasNext", hasNext);
        map.put("hasPrevious", hasPrevious);
        return map;
    }

    @Override
    public void postOne(Post post) {
        if (post == null){
            throw new IllegalArgumentException("帖子不能为空！");
        }

        // 将文本中的特殊字符转译成普通字符
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        // 向数据库中插入
        baseMapper.insert(post);
    }

    @Override
    public Post getPostDetail(Integer id) {
        Post post = baseMapper.selectById(id);
        return post;
    }

    @Override
    public void updateCommentCount(Integer id, Integer newCount) {
        Post post = new Post();
        post.setId(id);
        post.setCommentCount(newCount);
        baseMapper.updateById(post);
    }

    @Override
    public Post updateType(Integer id, Integer type) {
        Post post = new Post();
        post.setId(id);
        post.setType(type);
        baseMapper.updateById(post);
        return getPostDetail(id);
    }

    @Override
    public Post updateStatus(Integer id, Integer status) {
        Post post = new Post();
        post.setId(id);
        post.setStatus(status);
        baseMapper.updateById(post);
        return getPostDetail(id);
    }

    @Override
    public void updateScore(Integer id, Double score) {
        Post post = new Post();
        post.setId(id);
        post.setScore(score);
        baseMapper.updateById(post);
    }
}
