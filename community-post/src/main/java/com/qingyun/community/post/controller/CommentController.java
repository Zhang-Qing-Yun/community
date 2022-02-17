package com.qingyun.community.post.controller;


import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.pojo.Event;
import com.qingyun.community.base.utils.Constant;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.RedisKeyUtils;
import com.qingyun.community.post.feignClient.MessageClient;
import com.qingyun.community.post.feignClient.SearchClient;
import com.qingyun.community.post.pojo.Comment;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.service.CommentService;
import com.qingyun.community.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 張青云
 * @since 2021-05-08
 */
@Controller
@RequestMapping("/community/post/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageClient messageClient;

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private RedisTemplate redisTemplate;


    @PostMapping("/addComment/{postId}")
    @LoginRequired
    public String addComment(@PathVariable Integer postId, Comment comment) {
        comment.setUserId(hostHolder.get().getId());
        comment.setStatus(0);
        commentService.addComment(comment);


        // 触发评论事件
        Event event = new Event()
                .setTopic(Constant.TOPIC_COMMENT)
                .setUserId(hostHolder.get().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", postId);
        if (comment.getEntityType() == Constant.ENTITY_TYPE_POST) {  // 评论的是帖子
            Post target = postService.getPostDetail(comment.getEntityId());
            event.setEntityUserId(Integer.parseInt(target.getUserId()));
            //  更新ES
            searchClient.addPostToES(target);
            //  帖子评分发生变化
            String scoreKey = RedisKeyUtils.getPostScoreKey();
            redisTemplate.opsForSet().add(scoreKey, postId);
        } else if (comment.getEntityType() == Constant.ENTITY_TYPE_COMMENT) {  // 评论的是评论
            Comment target = commentService.getCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        messageClient.addEventToMq(event);

        //  TODO: 重定向路径写死为localhost了
        return "redirect:http://localhost:88/community/post/postDetail/" + postId;
    }
}

