package com.qingyun.community.message.component;

import com.qingyun.community.base.pojo.Event;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description：消息队列的生产者
 * @author: 張青云
 * @create: 2021-05-19 13:46
 **/
@Component
public class EventProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 处理事件
    public void fireEvent(Event event) {
        // 将事件发布到对应的队列里，路由键为event.getTopic()
        rabbitTemplate.convertAndSend("message-event-exchange", event.getTopic(), event);
    }
}
