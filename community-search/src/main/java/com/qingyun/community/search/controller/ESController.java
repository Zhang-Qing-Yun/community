package com.qingyun.community.search.controller;

import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.search.feignClient.LikeClient;
import com.qingyun.community.search.feignClient.UserClient;
import com.qingyun.community.search.pojo.Page;
import com.qingyun.community.search.pojo.Post;
import com.qingyun.community.search.service.ElasticsearchService;
import com.qingyun.community.search.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-22 18:42
 **/
@Controller
@RequestMapping("/search")
public class ESController {
    @Value("${page.size}")
    private int PAGE_SIZE;

    @Autowired
    private ElasticsearchService esService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private LikeClient likeClient;

    @GetMapping("/searchFromES")
    public String searchFromES(String keyword, Integer current, Page<Post> page, Model model) throws IOException {
        if (current == null || current <= 0) {
            current = 1;
        }
        Page<Post> postPage = esService.searchFromES(keyword, (current - 1) * PAGE_SIZE, PAGE_SIZE);
        List<Post> searchResult = postPage.getItems();  // 当前页数据

        //  封装结果
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (Post post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userClient.getUserById(Integer.parseInt(post.getUserId())));
                // 点赞数量
                map.put("likeCount", likeClient.getEntityLikeCount(Constant.ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setSize(PAGE_SIZE);
        long total = postPage.getTotal();
        page.setTotal(total);
        page.setPages(total%PAGE_SIZE == 0 ? total/PAGE_SIZE : total/PAGE_SIZE+1);
        page.setPath("/search/searchFromES?keyword=" + keyword);
        page.setCurrent(current);
        page.setTo();
        page.setFrom();

        return "/site/search";
    }

    @PostMapping("/addPostToES")
//    @LoginRequired  // 因为该接口只接受来自特定模块的请求，不会暴露给用户，所以没必要做校验
    @ResponseBody
    public void addPostToES(@RequestBody Post post) {
        esService.addPostToES(post);
    }

    @PostMapping("/deletePostFromES")
//    @LoginRequired  // 因为该接口只接受来自特定模块的请求，不会暴露给用户，所以没必要做校验
    @ResponseBody
    public void deletePostFromES(Integer id) {
        if (id != null) {
            esService.deletePostFromES(id);
        }
    }
}
