package com.qingyun.community.post.controller;

import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.utils.Constant;
import com.qingyun.community.post.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-28 13:50
 **/
@Controller
@RequestMapping("/community/post/data")
public class DataController {
    @Autowired
    private DataService dataService;

    @GetMapping("/addUV")
    @ResponseBody
    public void addUV(@RequestParam("ip") String ip) {
        dataService.addUV(ip);
    }

    @GetMapping("/recordDAU")
    @ResponseBody
    public void recordDAU(@RequestParam("userId")int userId) {
        dataService.recordDAU(userId);
    }

    @PostMapping("/calculateUV")
    @LoginRequired(Constant.AUTHORITY_ADMIN)
    public String calculateUV(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end, Model model) {
        long uv = dataService.calculateUV(start.atTime(LocalTime.now()), end.atTime(LocalTime.now()));
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", Date.from(start.atStartOfDay(ZoneOffset.ofHours(8)).toInstant()));
        model.addAttribute("uvEndDate", Date.from(end.atStartOfDay(ZoneOffset.ofHours(8)).toInstant()));
        return "/site/admin/data";
    }

    @PostMapping("/calculateDAU")
    @LoginRequired(Constant.AUTHORITY_ADMIN)
    public String calculateDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end, Model model) {
        long dau = dataService.calculateDAU(start.atTime(LocalTime.now()), end.atTime(LocalTime.now()));
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", Date.from(start.atStartOfDay(ZoneOffset.ofHours(8)).toInstant()));
        model.addAttribute("dauEndDate", Date.from(end.atStartOfDay(ZoneOffset.ofHours(8)).toInstant()));
        return "/site/admin/data";
    }

    @GetMapping("/dataPage")
    @LoginRequired(Constant.AUTHORITY_ADMIN)
    public String dataPage() {
        return "/site/admin/data";
    }
}
