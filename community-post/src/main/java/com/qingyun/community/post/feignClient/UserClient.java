package com.qingyun.community.post.feignClient;

import com.qingyun.community.post.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-03 10:56
 **/
@Component
@FeignClient("community-user")
public interface UserClient {
    @GetMapping("/user/getUserById/{userId}")
    @ResponseBody
    public User getUserById(@PathVariable(value = "userId") Integer userId);

}
