package com.qingyun.community.post.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyun.community.base.component.SensitiveFilter;
import com.qingyun.community.base.utils.RedisKeyUtils;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.mapper.PostMapper;
import com.qingyun.community.post.service.PostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Value("${cache.post.ttl}")
    private int POST_TTL;

    @Value("${flush-post-score-time}")
    private int FLUSH_SCORE_TIME;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private RedisTemplate redisTemplate;


    public List<Post> getPost(Integer current, Integer userId, Integer orderMode){
        //  按时间排序直接查数据库，因为是实时的数据
        //  某人的发帖列表直接查数据库
        if (orderMode == 0 || userId != null) {
            return getPostFromDB(current, userId, orderMode);
        }
        String redisKey = RedisKeyUtils.getPostIndex(current);
        String mapJson = (String) redisTemplate.opsForValue().get(redisKey);
        //  缓存里没有，去查数据库并放到缓存里
        if (StringUtils.isEmpty(mapJson)) {
            List<Post> map = getPostFromDB(current, userId, orderMode);
            redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(map), FLUSH_SCORE_TIME, TimeUnit.MINUTES);
            return map;
        }
        return JSON.parseObject(mapJson, new TypeReference<List<Post>>(){});
    }

    //  从数据库查询列表
    private List<Post> getPostFromDB(Integer current, Integer userId, Integer orderMode) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        Page<Post> page = new Page<Post>(current, PAGE_SIZE).setOptimizeCountSql(false);
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
        return page.getRecords();
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
    public Integer getTotalPost() {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.ne("status", 2);
        return baseMapper.selectCount(wrapper);
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
