package com.qingyun.community.post.quartz;

import com.qingyun.community.base.utils.Constant;
import com.qingyun.community.base.utils.RedisKeyUtils;
import com.qingyun.community.post.feignClient.LikeClient;
import com.qingyun.community.post.feignClient.SearchClient;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.service.PostService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @description： 更新帖子分数
 * @author: 張青云
 * @create: 2021-05-28 21:39
 **/
public class PostScoreRefreshJob extends QuartzJobBean implements Constant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PostService postService;

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private LikeClient likeClient;

    //  网站纪元
    private static final LocalDate era = LocalDate.of(2021, 6, 1);


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtils.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            return;
        }
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
    }

    /**
     * 更新一个帖子的评分，计算公式为：log(精华75+评论数*10+点赞数*2)+距离网站纪元的天数
     * @param postId
     */
    private void refresh(int postId) {
        Post post = postService.getPostDetail(postId);


        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeClient.getEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - era.atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()) / (1000.0 * 3600 * 24);
        // 更新帖子分数
        postService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        searchClient.addPostToES(post);
    }
}
