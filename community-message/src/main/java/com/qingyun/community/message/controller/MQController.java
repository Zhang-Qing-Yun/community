package com.qingyun.community.message.controller;

import com.qingyun.community.base.pojo.Event;
import com.qingyun.community.message.component.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-19 14:43
 **/
@Controller
@RequestMapping("/message/mq")
public class MQController {

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/addEventToMq")
    @ResponseBody
    public void addEventToMq(@RequestBody Event event) {
        eventProducer.fireEvent(event);
    }
}
