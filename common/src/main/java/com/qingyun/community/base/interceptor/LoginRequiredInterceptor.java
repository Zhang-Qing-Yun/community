package com.qingyun.community.base.interceptor;

import com.alibaba.fastjson.JSON;
import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.pojo.User;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
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
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            String xRequestedWith = request.getHeader("x-requested-with");

            //  通过反射来获取并处理注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            User user = hostHolder.get();
            if (loginRequired != null && user == null){  // 未登录
                //  异步请求
                if ("XMLHttpRequest".equals(xRequestedWith)) {
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(JSON.toJSONString(R.error().message("您当前还未登录！")));
                } else {
                    //  TODO: 这里的重定向路径写死为localhost了
                    response.sendRedirect("http://localhost:88/user/login");
                }
                return false;
            } else if (loginRequired != null) {  // 已登录，检查权限是否够
                int real = user.getType();  //  当前登录用户的真实权限
                int need = loginRequired.value();  // 用户要访问的方法需要的权限
                //  real < need 说明权限不够
                if(real < need) {
                    //  异步请求
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(JSON.toJSONString(R.error().message("暂无权限！")));
                    } else {
                        //  TODO: 这里的重定向路径写死为localhost了
                        response.sendRedirect("http://localhost:88/user/login");
                    }
                    return false;
                }
            }
        }

        //  放行
        return true;
    }
}
