package com.qingyun.community.base.handle;

import com.qingyun.community.base.utils.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @description：全局异常处理
 * @author: 張青云
 * @create: 2021-05-02 19:52
 **/

@ControllerAdvice  // Controller的全局配置类
public class GlobalExceptionHandler {

    //  出现异常返回R.error()的Json数据
//    @ExceptionHandler(Exception.class)
//    @ResponseBody
//    public R error(Exception e){
//        e.printStackTrace();
//        return R.error().message("执行了全局自定义异常");
//    }

    //  出现错误返回错误页面
    @ExceptionHandler(Exception.class)
    public String error(Exception e){
        e.printStackTrace();
        return "/error/500";
    }
}
