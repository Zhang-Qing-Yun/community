package com.qingyun.community.message.controller;

import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.R;
import com.qingyun.community.message.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-11 12:46
 **/
@Controller
@RequestMapping("/message/like")
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/likeOne")
    @ResponseBody
    @LoginRequired
    public R likeOne(int entityType, int entityId, int entityUserId) {
        //  获取当前登陆对象
        User user  = hostHolder.get();

        //  执行点赞动作
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //  点赞数
        long likeCount = likeService.getEntityLikeCount(entityType,entityId);
        //  当前的点赞状态
        int likeStatus = likeService.getEntityLikeStatus(user.getId(),entityType,entityId);

        Map<String,Object> map  = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return R.ok().data(map);
    }

    @GetMapping("/getEntityLikeCount")
    @ResponseBody
    public long getEntityLikeCount(int entityType, int entityId) {
        return likeService.getEntityLikeCount(entityType, entityId);
    }

    @GetMapping("/getEntityLikeStatus")
    @ResponseBody
    public int getEntityLikeStatus(int userId, int entityType, int entityId) {
        return likeService.getEntityLikeStatus(userId, entityType, entityId);
    }

    @GetMapping("/getUserLikeCount")
    @ResponseBody
    public int getUserLikeCount(int userId) {
        return likeService.getUserLikeCount(userId);
    }
}
