package com.qingyun.community.user.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-11 15:47
 **/
@Component
@FeignClient("community-message")
public interface LikeClient {
    @GetMapping("/message/like/getEntityLikeCount")
    @ResponseBody
    public long getEntityLikeCount(@RequestParam("entityType")int entityType, @RequestParam("entityId")int entityId);

    @GetMapping("/message/like/getEntityLikeStatus")
    @ResponseBody
    public int getEntityLikeStatus(@RequestParam("userId")int userId, @RequestParam("entityType")int entityType, @RequestParam("entityId")int entityId);

    @GetMapping("/message/like/getUserLikeCount")
    @ResponseBody
    public int getUserLikeCount(@RequestParam("userId")int userId);

    @PostMapping("/message/follow/getFolloweeCount")
    @ResponseBody
    public long getFolloweeCount(@RequestParam("userId")int userId, @RequestParam("entityType")int entityType);

    @PostMapping("/message/follow/getFollowerCount")
    @ResponseBody
    public long getFollowerCount(@RequestParam("entityType")int entityType, @RequestParam("entityId")int entityId);

    @PostMapping("/message/follow/hasFollowed")
    @ResponseBody
    public boolean hasFollowed(@RequestParam("userId")int userId, @RequestParam("entityType")int entityType, @RequestParam("entityId")int entityId);
}
