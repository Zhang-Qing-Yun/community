package com.qingyun.community.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qingyun.community.base.component.SensitiveFilter;
import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.message.pojo.Message;
import com.qingyun.community.message.mapper.MessageMapper;
import com.qingyun.community.message.service.MessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-09
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Message> getMessagesByUserId(Integer userId, Integer offset, Integer limit) {
        return baseMapper.getMessagesByUserId(userId, offset, limit);
    }

    @Override
    public int getConversationCount(Integer userId) {
        return baseMapper.getConversationCount(userId);
    }

    @Override
    public List<Message> getLetters(String conversationId, Integer offset, Integer limit) {
        return baseMapper.getLetters(conversationId, offset, limit);
    }

    @Override
    public int getConversationCountById(String conversationId) {
        return baseMapper.getConversationCountById(conversationId);
    }

    @Override
    public int getUnReadLettersCount(Integer userId, String conversationId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.ne("from_id", 1);
        wrapper.eq("to_id", userId);
        if(conversationId != null) {
            wrapper.eq("conversation_id", conversationId);
        }
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return baseMapper.insert(message);
    }

    @Override
    public void updateMessageStatus(List<Integer> ids, int status) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        Message message = new Message();
        message.setStatus(status);
        baseMapper.update(message, wrapper);
    }

}
