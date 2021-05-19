package com.qingyun.community.user.feignClient;

import com.qingyun.community.base.annotation.LoginRequired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-19 21:01
 **/
@Component
@FeignClient(value = "community-message", contextId = "messageClient")
public interface MessageClient {
    @GetMapping("/message/getUnreadCount")
    @LoginRequired
    @ResponseBody
    public int getUnreadCount();
}
