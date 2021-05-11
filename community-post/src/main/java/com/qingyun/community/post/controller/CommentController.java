package com.qingyun.community.post.controller;


import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.post.pojo.Comment;
import com.qingyun.community.post.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private HostHolder hostHolder;

    @PostMapping("/addComment/{postId}")
    @LoginRequired
    public String addComment(@PathVariable Integer postId, Comment comment) {
        comment.setUserId(hostHolder.get().getId());
        comment.setStatus(0);
        commentService.addComment(comment);

        //  TODO: 重定向路径写死为localhost了
        return "redirect:http://localhost:88/community/post/postDetail/" + postId;
    }
}

