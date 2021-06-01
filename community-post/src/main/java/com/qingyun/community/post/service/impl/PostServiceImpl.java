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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
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

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public List<Post> getPost(Integer current, Integer userId, Integer orderMode){
        //  按时间排序直接查数据库，因为是实时的数据
        //  某人的发帖列表直接查数据库
        if (orderMode == 0 || userId != null) {
            return getPostFromDB(current, userId, orderMode);
        }
        String redisKey = RedisKeyUtils.getPostIndex(current);
        String json = (String) redisTemplate.opsForValue().get(redisKey);
        //  缓存里没有，去查数据库并放到缓存里
        if (StringUtils.isEmpty(json)) {
            return getPostWithLock(current, userId, orderMode);
        }
        return JSON.parseObject(json, new TypeReference<List<Post>>(){});
    }

    /**
     * 从数据库查询列表
     * @param current
     * @param userId
     * @param orderMode
     * @return
     */
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

    /**
     * 手写基于redis的分布式互斥锁，解决缓存击穿和缓存雪崩
     * @param current
     * @param userId
     * @param orderMode
     * @return
     */
    private List<Post> getPostWithLock(Integer current, Integer userId, Integer orderMode) {
        //  设置uuid，防止发生锁误删
        String uuid = UUID.randomUUID().toString();
        //  此处过期时间需要设置的长一点，必须大于业务的执行时间，否则业务还在执行时锁过期
        //  加锁时保证原子性
        Boolean success = redisTemplate.opsForValue().setIfAbsent(RedisKeyUtils.getPostIndexLock(current), uuid, 10, TimeUnit.SECONDS);
        if (success) {  // 加锁成功
            try {
                List<Post> list = getPostFromDB(current, userId, orderMode);
                String redisKey = RedisKeyUtils.getPostIndex(current);
                //  添加到缓存
                redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(list), FLUSH_SCORE_TIME, TimeUnit.MINUTES);
                return list;
            } finally {  // 解锁
                //  使用Lua脚本保证解锁的原子性
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                redisTemplate.execute(new DefaultRedisScript(script, Long.class),
                        Arrays.asList(RedisKeyUtils.getPostIndexLock(current)), uuid);
            }
        } else {
            //  加锁不成功则采用自旋的方式
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getPost(current, userId, orderMode);
        }
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
        //  先查缓存
        String redisKey = RedisKeyUtils.getPostDetail(id);
        String json = (String) redisTemplate.opsForValue().get(redisKey);
        //  缓存里没有，去查数据库并放到缓存里
        if (StringUtils.isEmpty(json)) {
            return getPostDetailWithLock(id);
        }
        return JSON.parseObject(json, Post.class);
    }

    /**
     * 使用Redisson提供的分布式锁来解决加载帖子详情到缓存中时的缓存雪崩和缓存击穿
     * @param id 帖子id
     * @return 帖子详情
     */
    private Post getPostDetailWithLock(Integer id) {
        //  获取锁
        //  Redisson锁有三个优点：1.不会出现误删锁；2.锁有过期时间，不会出现死锁；3.锁自动续期，不会出现业务超时。
        RLock lock = redissonClient.getLock(RedisKeyUtils.getPostDetailLock(id));
        //  加锁
        boolean lockSuccess = false;
        try {
            lockSuccess = lock.tryLock(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (lockSuccess) {
            try {
                //  查数据库
                Post post = baseMapper.selectById(id);
                //  随机化过期时间（单位为秒），解决缓存雪崩问题
                int ttl = new Random().nextInt(60) + POST_TTL*60;
                //  添加到缓存里
                redisTemplate.opsForValue().set(RedisKeyUtils.getPostDetail(id), JSON.toJSONString(post),
                        ttl, TimeUnit.SECONDS);
                return post;
            } finally {
                //  解锁
                lock.unlock();
            }
        } else {
            //  获取不到锁时自旋
            return getPostDetail(id);
        }
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
        //  失效模式+分布式读写锁来保证缓存一致性
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
