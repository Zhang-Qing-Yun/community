package com.qingyun.community.message.controller;


import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.pojo.Page;
import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.R;
import com.qingyun.community.message.feignClient.UserClient;
import com.qingyun.community.message.pojo.Message;
import com.qingyun.community.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
 * @since 2021-05-09
 */
@Controller
@RequestMapping("/message")
public class MessageController {

    @Value("${page.size}")
    private int PAGE_SIZE;

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserClient userClient;

    /**
     * 获取当前私信页未读消息的id列表
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if ((int)hostHolder.get().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    /**
     * 与当前用户私信的用户
     * @param conversationId 会话id
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.get().getId() == id0) {
            return userClient.getUserById(id1);
        } else {
            return userClient.getUserById(id0);
        }
    }

    @GetMapping("/letterList")
    @LoginRequired
    public String getLetterList(Model model, Page page, @RequestParam(required = false) Integer current) {
        User user = hostHolder.get();
        //  设置分页信息
        if(current == null || current < 1) {
            current = 1;
        }
        int total = messageService.getConversationCount(user.getId());
        int pages = total%PAGE_SIZE == 0 ? total/PAGE_SIZE : total/PAGE_SIZE+1;
        page.setCurrent(current);
        page.setSize(PAGE_SIZE);
        page.setPath("/message/letterList");
        page.setTotal(total);
        page.setPages(pages);  // 设置总页数
        page.setTo();
        page.setFrom();

        //  会话列表
        List<Message> conversationList = messageService.getMessagesByUserId(user.getId(), (page.getCurrent() - 1) * PAGE_SIZE, PAGE_SIZE);
        //  封装结果
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.getConversationCountById(message.getConversationId()));
                map.put("unreadCount", messageService.getUnReadLettersCount(user.getId(), message.getConversationId()));
                int targetId = (int)user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userClient.getUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.getUnReadLettersCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    @GetMapping("/letterDetail/{conversationId}")
    @LoginRequired
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page, @RequestParam(required = false) Integer current) {
        //  设置分页信息
        if(current == null || current < 1) {
            current = 1;
        }
        int total = messageService.getConversationCountById(conversationId);  // 该会话私信的数量
        int pages = total%PAGE_SIZE == 0 ? total/PAGE_SIZE : total/PAGE_SIZE+1;
        page.setCurrent(current);
        page.setSize(PAGE_SIZE);
        page.setPath("/message/letterDetail/" + conversationId);
        page.setTotal(total);
        page.setPages(pages);  // 设置总页数
        page.setTo();
        page.setFrom();

        // 私信列表
        List<Message> letterList = messageService.getLetters(conversationId, (page.getCurrent() - 1) * PAGE_SIZE, PAGE_SIZE);
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userClient.getUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.updateMessageStatus(ids, 1);
        }

        return "/site/letter-detail";
    }

    @PostMapping("/sendLetter")
    @LoginRequired
    @ResponseBody
    public R sendLetter(String toName, String content) {
        User target = userClient.getUserByUsername(toName);
        if (target == null) {
            return R.error().message("目标用户不存在!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.get().getId());
        message.setToId(target.getId());
        message.setStatus(0);
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        messageService.addMessage(message);

        return R.ok();
    }
}

