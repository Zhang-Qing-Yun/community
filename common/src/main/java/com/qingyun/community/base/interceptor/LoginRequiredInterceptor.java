package com.qingyun.community.base.interceptor;

import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @description 拦截器，判断是否是方法且标注了@LoginRequired注解，如果是则判断ThreadLocal里是否包含User信息
 * @author: 張青云
 * @create: 2021-05-05 23:03
 **/
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            LoginRequired loginRequired =method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && hostHolder.get() == null){
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }

        return true;
    }
}
