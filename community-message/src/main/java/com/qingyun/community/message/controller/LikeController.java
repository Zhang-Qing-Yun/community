package com.qingyun.community.message.controller;

import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.pojo.Event;
import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.Constant;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.R;
import com.qingyun.community.message.component.EventProducer;
import com.qingyun.community.message.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/likeOne")
    @ResponseBody
    @LoginRequired
    public R likeOne(int entityType, int entityId, int entityUserId, int postId) {
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

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(Constant.TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        return R.ok().data(map);
    }

    @GetMapping("/getEntityLikeCount")
    @ResponseBody
    public long getEntityLikeCount(@RequestParam("entityType")int entityType,
                                   @RequestParam("entityId")int entityId) {
        return likeService.getEntityLikeCount(entityType, entityId);
    }

    @GetMapping("/getEntitiesLikeCount")
    @ResponseBody
    public List<Long> getEntitiesLikeCount(@RequestParam("entityType") int entityType,
                                           @RequestParam("entityIds") List<Integer> entityIds) {
        return likeService.getEntitiesLikeCount(entityType, entityIds);
    }

    @GetMapping("/getEntityLikeStatus")
    @ResponseBody
    public int getEntityLikeStatus(@RequestParam("userId")int userId, @RequestParam("entityType")int entityType,
                                   @RequestParam("entityId")int entityId) {
        return likeService.getEntityLikeStatus(userId, entityType, entityId);
    }


    @GetMapping("/getEntitiesLikeStatus")
    @ResponseBody
    public List<Integer> getEntitiesLikeStatus(@RequestParam("userId") int userId, @RequestParam("entityType") int entityType,
                                               @RequestParam("entityIds") List<Integer> entityIds) {
        return likeService.getEntitiesLikeStatus(userId, entityType, entityIds);
    }

    @GetMapping("/getUserLikeCount")
    @ResponseBody
    public int getUserLikeCount(int userId) {
        return likeService.getUserLikeCount(userId);
    }
}
