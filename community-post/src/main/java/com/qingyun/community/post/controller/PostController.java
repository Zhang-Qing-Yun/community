package com.qingyun.community.post.controller;


import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.exception.CommunityException;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.R;
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
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @GetMapping("/index")
    public String getIndexPage(Model model, @RequestParam(required = false) Integer current) {
        //  当前页的全部帖子
        Map<String, Object> res = postService.getPost(current, null);
        List<Post> items = (List<Post>) res.get("items");

        //  封装分页对象
        Page page = new Page();
        page.setCurrent((Integer) res.get("current"));
        page.setHasNext((Boolean) res.get("hasNext"));
        page.setHasPrevious((Boolean) res.get("hasPrevious"));
        page.setTotal((Long) res.get("total"));
        page.setSize((Long) res.get("pageSize"));  // 每页记录数
        page.setPages((Long) res.get("pages"));  // 总页数
        page.setTo();
        page.setFrom();
        page.setPath("/community/post/index");

        List<Map<String, Object>> posts = new ArrayList<>();
        if(items != null && items.size() != 0) {
            for(Post post: items) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userClient.getUserById(Integer.parseInt(post.getUserId()));
                map.put("user", user);
                long likeCount = likeClient.getEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                posts.add(map);
            }
        }
        model.addAttribute("posts", posts);
        model.addAttribute("page", page);
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
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userClient.getUserById(comment.getUserId()));
                // 评论的点赞数
                likeCount = likeClient.getEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount", likeCount);
                // 当前用户对评论的点赞状态
                likeStatus = hostHolder.get()==null?0:likeClient.getEntityLikeStatus(hostHolder.get().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);
                // 回复列表
                Map<String, Object> replyMap = commentService.getCommentByEntityId(null, ENTITY_TYPE_COMMENT, comment.getId());
                List<Comment> replyList = (List<Comment>) replyMap.get("items");
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userClient.getUserById(reply.getUserId()));
                        // 回复目标
                        User target = null;
                        if (reply.getTargetId() != null && reply.getTargetId() != 0) {
                            target = userClient.getUserById(reply.getTargetId());
                        }
                        replyVo.put("target", target);
                        // 回复的点赞数
                        likeCount = likeClient.getEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 当前用户对回复的点赞状态
                        likeStatus = hostHolder.get() == null ? 0 : likeClient.getEntityLikeStatus(hostHolder.get().getId(),ENTITY_TYPE_COMMENT,reply.getId());
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

        // TODO: 增加帖子回复等其它内容
        return "/site/discuss-detail";
    }
}

