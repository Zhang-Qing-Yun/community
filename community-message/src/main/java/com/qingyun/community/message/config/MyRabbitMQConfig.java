package com.qingyun.community.message.config;

import com.qingyun.community.base.utils.Constant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-19 12:44
 **/
@Configuration
public class MyRabbitMQConfig {
    @Bean
    //  使用JSON来序列化消息内容
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 交给spring管理后，rabbitmq没有的话就会自动创建，有的话即使是属性发生了变化也不会覆盖掉
     * @return
     */
    @Bean
    //  普通队列，评论事件
    public Queue commentEventQueue() {
        return new Queue("comment.event.queue", true, false, false);
    }

    @Bean
    //  普通队列，点赞事件
    public Queue likeEventQueue() {
        return new Queue("like.event.queue", true, false, false);
    }

    @Bean
    //  普通队列，关注事件
    public Queue followEventQueue() {
        return new Queue("follow.event.queue", true, false, false);
    }

    @Bean
    //  Topic类型的交换机
    public Exchange messageEventExchange() {
        return new TopicExchange("message-event-exchange", true, false);
    }

    @Bean
    //  将队列绑定给交换机
    public Binding commentEventBinding() {
        return new Binding("comment.event.queue", Binding.DestinationType.QUEUE,
                "message-event-exchange", Constant.TOPIC_COMMENT, null);
    }

    @Bean
    //  将队列绑定给交换机
    public Binding likeEventBinding() {
        return new Binding("like.event.queue", Binding.DestinationType.QUEUE,
                "message-event-exchange", Constant.TOPIC_LIKE, null);
    }

    @Bean
    //  将队列绑定给交换机
    public Binding followEventBinding() {
        return new Binding("follow.event.queue", Binding.DestinationType.QUEUE,
                "message-event-exchange", Constant.TOPIC_FOLLOW, null);
    }
}
