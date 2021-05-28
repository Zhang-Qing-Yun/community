package com.qingyun.community.post.config;

import com.qingyun.community.base.interceptor.LoginRequiredInterceptor;
import com.qingyun.community.base.interceptor.LoginUserInterceptor;
import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.post.feignClient.MessageClient;
import com.qingyun.community.post.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-05 15:02
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageClient messageClient;

    @Autowired
    private DataService dataService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg",
                        "/user/kaptcha", "/user/register", "/user/login", "/user/api_login");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
                User user = hostHolder.get();
                if (user != null && modelAndView != null) {
                    modelAndView.addObject("allUnreadCount", messageClient.getUnreadCount());
                }
            }
        }).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        //  统计
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // 统计UV
                String ip = request.getRemoteHost();
                dataService.addUV(ip);

                // 统计DAU
                User user = hostHolder.get();
                if (user != null) {
                    dataService.recordDAU(user.getId());
                }

                return true;
            }
        }).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
