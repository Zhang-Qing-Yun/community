package com.qingyun.community.message.controller;

import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.pojo.Event;
import com.qingyun.community.base.pojo.Page;
import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.Constant;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.R;
import com.qingyun.community.message.component.EventProducer;
import com.qingyun.community.message.feignClient.UserClient;
import com.qingyun.community.message.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-12 14:14
 **/
@Controller
@RequestMapping("/message/follow")
public class FollowController implements Constant {
    @Value("${page.size}")
    private int PAGE_SIZE;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserClient userClient;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 判断当前用户是否关注了某个用户
     * @param userId
     * @return
     */
    private boolean hasFollowed(int userId) {
        if (hostHolder.get() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.get().getId(), ENTITY_TYPE_USER, userId);
    }

    @PostMapping("/followOne")
    @ResponseBody
    @LoginRequired
    public R follow(int entityType, int entityId) {
        User user = hostHolder.get();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return R.ok().message("关注成功");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    @LoginRequired
    public R unfollow(int entityType, int entityId) {
        User user = hostHolder.get();
        followService.unfollow(user.getId(), entityType, entityId);
        return R.ok().message("取消关注成功");
    }

    @PostMapping("/getFolloweeCount")
    @ResponseBody
    public long getFolloweeCount(int userId, int entityType) {
        return followService.getFolloweeCount(userId, entityType);
    }

    @PostMapping("/getFollowerCount")
    @ResponseBody
    public long getFollowerCount(int entityType, int entityId) {
        return followService.getFollowerCount(entityType, entityId);
    }

    @PostMapping("/hasFollowed")
    @ResponseBody
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        return followService.hasFollowed(userId, entityType, entityId);
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, @RequestParam(required = false) Integer current,
                               Page page, Model model) {
        User user = userClient.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        //  设置分页信息
        if(current == null || current < 1) {
            current = 1;
        }
        page.setSize(PAGE_SIZE);
        page.setPath("/message/follow/followees/" + userId);
        long total = followService.getFolloweeCount(userId, ENTITY_TYPE_USER);
        page.setTotal(total);
        page.setPages(total%PAGE_SIZE == 0 ? total/PAGE_SIZE : total/PAGE_SIZE+1);
        page.setCurrent(current);
        page.setTo();
        page.setFrom();

        List<Map<String, Object>> userList = followService.getFollowees(userId, (page.getCurrent() - 1) * PAGE_SIZE, PAGE_SIZE);
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, @RequestParam(required = false) Integer current,
                               Page page, Model model) {
        User user = userClient.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        //  设置分页信息
        if(current == null || current < 1) {
            current = 1;
        }
        page.setSize(PAGE_SIZE);
        page.setPath("/message/follow/followers/" + userId);
        long total = followService.getFollowerCount(ENTITY_TYPE_USER, userId);
        page.setTotal(total);
        page.setPages(total%PAGE_SIZE == 0 ? total/PAGE_SIZE : total/PAGE_SIZE+1);
        page.setCurrent(current);
        page.setTo();
        page.setFrom();

        List<Map<String, Object>> userList = followService.getFollowers(userId, (page.getCurrent() - 1) * PAGE_SIZE, PAGE_SIZE);
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }
}
