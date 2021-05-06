package com.qingyun.community.third.controller;

import com.qingyun.community.third.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-06 14:45
 **/
@Controller
@RequestMapping("/third")
public class OssController {

    @Autowired
    private OssService ossService;

    @GetMapping("/oss/token")
    @ResponseBody
    public Map<String, String> getTokenOfOss() {
        Map<String, String> tokenOfOss = ossService.getTokenOfOss();
        return tokenOfOss;
    }

}
