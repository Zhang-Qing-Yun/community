package com.qingyun.community.base.interceptor;

import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-05-05 13:29
 **/
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder threadLocal;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //  查询是否登陆过
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        if(user != null) {
            threadLocal.set(user);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = threadLocal.get();
        if (user!=null&& modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
    }
}
