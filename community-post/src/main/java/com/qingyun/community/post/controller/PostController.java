package com.qingyun.community.post.controller;


import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.exception.CommunityException;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.R;
import com.qingyun.community.base.utils.RedisKeyUtils;
import com.qingyun.community.post.feignClient.LikeClient;
import com.qingyun.community.post.feignClient.SearchClient;
import com.qingyun.community.post.feignClient.UserClient;
import com.qingyun.community.post.pojo.Comment;
import com.qingyun.community.post.pojo.Page;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.pojo.User;
import com.qingyun.community.post.service.CommentService;
import com.qingyun.community.post.service.PostService;
import com.qingyun.community.base.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.thymeleaf.expression.Ids;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 張青云
 * @since 2021-05-02
 */
@Controller
@RequestMapping("/community/post")
public class PostController implements Constant {
    @Value("${page.size}")
    private int PAGE_SIZE;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private LikeClient likeClient;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/index")
    public String getIndexPage(Model model, @RequestParam(required = false) Integer current,
                               @RequestParam(required = false) Integer orderMode) {
        if(current == null || current <= 1) {
            current = 1;
        }
        if (orderMode == null || (orderMode != 0 && orderMode != 1)) {
            orderMode = 0;
        }
        //  当前页的全部帖子
        List<Post> items = postService.getPost(current, null, orderMode);

        //  封装分页对象
        Page page = new Page();
        page.setCurrent(current);
        int total = postService.getTotalPost();
        int pages = total%PAGE_SIZE == 0 ? total/PAGE_SIZE : total/PAGE_SIZE+1;
        page.setTotal(total);
        page.setSize(PAGE_SIZE);  // 每页记录数
        page.setPages(pages);  // 总页数
        page.setTo();
        page.setFrom();
        page.setPath("/community/post/index?orderMode="+orderMode);

        List<Map<String, Object>> posts = new ArrayList<>();
        if(items != null && items.size() != 0) {
            List<Integer> userIds = new ArrayList<>();
            List<Integer> postIds = new ArrayList<>();
            for(Post post: items) {
                userIds.add(Integer.parseInt(post.getUserId()));
                postIds.add(post.getId());
            }
            //  查询post的作者信息
            List<User> users = userClient.getUsersByIds(userIds);
            //  查询post的点赞数量
            List<Long> entitiesLikeCount = likeClient.getEntitiesLikeCount(ENTITY_TYPE_POST, postIds);
            for (int i = 0; i < items.size(); i++)   {
                Post post = items.get(i);
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = users.get(i);
                map.put("user", user);
                long likeCount = entitiesLikeCount.get(i);
                map.put("likeCount",likeCount);
                posts.add(map);
            }
        }
        model.addAttribute("posts", posts);
        model.addAttribute("page", page);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }


    @PostMapping("/postOne")
    @ResponseBody
    @Transactional
    @LoginRequired
    public R postOne(String title, String content, HttpServletRequest request){
        //  获取当前登录对象
        com.qingyun.community.base.pojo.User user = hostHolder.get();
        //  创建post对象
        Post post = new Post();
        post.setUserId(String.valueOf(user.getId()));
        post.setTitle(title);
        post.setContent(content);
        post.setCommentCount(0);
        post.setScore(0.0);
        post.setType(0);
        post.setStatus(0);

        try {
            postService.postOne(post);
            //  插入到ES中
            searchClient.addPostToES(post);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(20001, "发布失败，请重试！");
        }
        //  帖子评分发生变化
        String scoreKey = RedisKeyUtils.getPostScoreKey();
        try {
            redisTemplate.opsForSet().add(scoreKey, post.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok().message("发布成功！");
    }

    @GetMapping("postDetail/{id}")
    public String getPostDetail(@PathVariable Integer id, Model model, @RequestParam(required = false) Integer current) {
        Post post = postService.getPostDetail(id);
        User user = userClient.getUserById(Integer.parseInt(post.getUserId()));
        model.addAttribute("post", post);
        model.addAttribute("user", user);

        //  帖子的点赞数
        long likeCount = likeClient.getEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        //  当前用户对该帖子的点赞状态
        int likeStatus = hostHolder.get() == null ? 0 : likeClient.getEntityLikeStatus(hostHolder.get().getId(),ENTITY_TYPE_POST, id);
        model.addAttribute("likeStatus",likeStatus);


        if(current == null) {
            current = 1;
        }
        Map<String, Object> commentPage = commentService.getCommentByEntityId(current, Constant.ENTITY_TYPE_POST, id);
        List<Comment> commentList = (List<Comment>) commentPage.get("items");  // 评论列表
        Page page = (Page) commentPage.get("page");  // 评论的分页
        page.setPath("/community/post/postDetail/" + id);

        //  封装每一条评论
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null && commentList.size() != 0) {
            List<Integer> userIds = new ArrayList<>();
            List<Integer> commentIds = new ArrayList<>();
            for(Comment comment: commentList) {
                userIds.add(comment.getUserId());
                commentIds.add(comment.getId());
            }
            //  查询comment的作者信息
            List<User> users = userClient.getUsersByIds(userIds);
            //  查询comment的点赞数量
            List<Long> entitiesLikeCount = likeClient.getEntitiesLikeCount(ENTITY_TYPE_COMMENT, commentIds);
            //  点赞状态，如果当前未登录则使用默认的初始化值0，否则去远程调用服务查询
            List<Integer> likeStatusList = null;
            if(hostHolder.get() != null) {  // 登录
                likeStatusList = likeClient.getEntitiesLikeStatus(hostHolder.get().getId(), ENTITY_TYPE_COMMENT, commentIds);
            } else {  // 未登录
                likeStatusList = Arrays.asList(new Integer[commentList.size()]);  // List里的元素都是null
            }
            for (int i = 0; i < commentList.size(); i++) {
                Comment comment = commentList.get(i);
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", users.get(i));
                // 评论的点赞数
                likeCount = entitiesLikeCount.get(i);
                commentVo.put("likeCount", likeCount);
                // 当前用户对评论的点赞状态
                likeStatus = likeStatusList.get(i) == null ? 0 : likeStatusList.get(i);
                commentVo.put("likeStatus",likeStatus);
                // 回复列表
                Map<String, Object> replyMap = commentService.getCommentByEntityId(null, ENTITY_TYPE_COMMENT, comment.getId());
                List<Comment> replyList = (List<Comment>) replyMap.get("items");
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null && replyList.size() != 0) {
                    List<Integer> replyUserIds = new ArrayList<>();
                    List<Integer> replyIds = new ArrayList<>();
                    for(Comment reply: replyList) {
                        replyUserIds.add(reply.getUserId());
                        replyIds.add(reply.getId());
                    }
                    //  远程调用，批量查询
                    List<User> replyUsers = userClient.getUsersByIds(replyUserIds);
                    List<Long> replyLikeCount = likeClient.getEntitiesLikeCount(ENTITY_TYPE_COMMENT, replyIds);
                    //  点赞状态，如果当前未登录则使用默认的初始化值0，否则远程调用服务查询
                    List<Integer> replyLikeStatusList = null;
                    if(hostHolder.get() != null) {  // 登录
                        replyLikeStatusList = likeClient.getEntitiesLikeStatus(hostHolder.get().getId(), ENTITY_TYPE_COMMENT, replyIds);
                    } else {  // 未登录
                        replyLikeStatusList = Arrays.asList(new Integer[commentList.size()]);  // List里的元素都是null
                    }

                    for(int j = 0; j < replyList.size(); j++) {
                        Comment reply = replyList.get(j);
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", replyUsers.get(j));
                        // 回复目标
                        User target = null;
                        if (reply.getTargetId() != null && reply.getTargetId() != 0) {
                            target = userClient.getUserById(reply.getTargetId());
                        }
                        replyVo.put("target", target);
                        // 回复的点赞数
                        likeCount = replyLikeCount.get(j);
                        replyVo.put("likeCount", likeCount);
                        // 当前用户对回复的点赞状态
                        likeStatus = replyLikeStatusList.get(j) == null ? 0 : replyLikeStatusList.get(j);
                        replyVo.put("likeStatus", likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                //  评论的回复数量
                commentVo.put("replyCount", replyList == null ? 0 : replyList.size());

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        model.addAttribute("page", page);

        return "/site/discuss-detail";
    }

    /**
     * 置顶
     * @param id 帖子id
     * @return 操作的结果
     */
    @PostMapping("/top")
    @ResponseBody
    @Transactional
    @LoginRequired(Constant.AUTHORITY_MODERATOR)  // 需要版主权限
    public R setTop(int id) {

        try {
            Post post = postService.updateType(id, 1);
            //  插入到ES中
            searchClient.addPostToES(post);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(20001, "操作失败，请重试！");
        }
        return R.ok().message("操作成功！");
    }

    /**
     * 加精
     * @param id 帖子id
     * @return 操作的结果
     */
    @PostMapping("/wonderful")
    @ResponseBody
    @Transactional
    @LoginRequired(Constant.AUTHORITY_MODERATOR)  // 需要版主权限
    public R setWonderful(int id) {

        try {
            Post post = postService.updateStatus(id, 1);
            //  插入到ES中
            searchClient.addPostToES(post);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(20001, "操作失败，请重试！");
        }
        //  帖子评分发生变化
        String scoreKey = RedisKeyUtils.getPostScoreKey();
        try {
            redisTemplate.opsForSet().add(scoreKey, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.ok().message("操作成功！");
    }

    /**
     * 删除
     * @param id 帖子id
     * @return 操作的结果
     */
    @PostMapping("/delete")
    @ResponseBody
    @Transactional
    @LoginRequired(Constant.AUTHORITY_ADMIN)  // 需要管理员权限
    public R delete(int id) {

        try {
            postService.updateStatus(id, 2);
            //  从ES当中删除
            searchClient.deletePostFromES(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommunityException(20001, "操作失败，请重试！");
        }
        return R.ok().message("操作成功！");
    }

}

