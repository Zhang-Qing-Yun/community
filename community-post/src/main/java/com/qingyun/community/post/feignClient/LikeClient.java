package com.qingyun.community.post.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-11 15:47
 **/
@Component
@FeignClient(value = "community-message", contextId = "likeClient")
public interface LikeClient {
    @GetMapping("/message/like/getEntityLikeCount")
    @ResponseBody
    public long getEntityLikeCount(@RequestParam("entityType")int entityType, @RequestParam("entityId")int entityId);

    @GetMapping("/message/like/getEntityLikeStatus")
    @ResponseBody
    public int getEntityLikeStatus(@RequestParam("userId")int userId, @RequestParam("entityType")int entityType, @RequestParam("entityId")int entityId);

    @GetMapping("/message/like/getEntitiesLikeCount")
    @ResponseBody
    public List<Long> getEntitiesLikeCount(@RequestParam("entityType") int entityType, @RequestParam("entityIds") List<Integer> entityIds);

    @GetMapping("/message/like/getEntitiesLikeStatus")
    @ResponseBody
    public List<Integer> getEntitiesLikeStatus(@RequestParam("userId") int userId, @RequestParam("entityType") int entityType, @RequestParam("entityIds") List<Integer> entityIds);

}
