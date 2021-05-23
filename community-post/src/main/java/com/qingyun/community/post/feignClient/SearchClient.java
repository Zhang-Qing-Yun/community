package com.qingyun.community.post.feignClient;

import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.post.pojo.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-22 20:29
 **/
@Component
@FeignClient("community-search")
public interface SearchClient {
    @PostMapping("/search/addPostToES")
    @LoginRequired
    @ResponseBody
    public void addPostToES(@RequestBody Post post);

    @PostMapping("/search/deletePostFromES")
    @LoginRequired
    @ResponseBody
    public void deletePostFromES(Integer id);
}
