package com.qingyun.community.post.controller;


import com.qingyun.community.post.feignClient.UserClient;
import com.qingyun.community.post.pojo.Page;
import com.qingyun.community.post.pojo.Post;
import com.qingyun.community.post.pojo.User;
import com.qingyun.community.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

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
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserClient userClient;

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
                posts.add(map);
            }
        }
        model.addAttribute("posts", posts);
        model.addAttribute("page", page);
        return "/index";
    }


}

