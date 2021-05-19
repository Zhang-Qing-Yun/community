package com.qingyun.community.message.component;

import com.alibaba.fastjson.JSONObject;
import com.qingyun.community.base.pojo.Event;
import com.qingyun.community.base.utils.Constant;
import com.qingyun.community.message.pojo.Message;
import com.qingyun.community.message.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description：消息队列的消费者
 * @author: 張青云
 * @create: 2021-05-19 13:52
 **/
@Component
@RabbitListener(queues = {"comment.event.queue", "like.event.queue", "follow.event.queue"})
public class EventConsumer {

    @Autowired
    private MessageService messageService;

    @RabbitHandler
    public void handleEventMessage(Event event) {
        if(event == null) {
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(Constant.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        message.setStatus(0);

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        //  向数据库插入一条消息
        messageService.addMessage(message);
    }
}
