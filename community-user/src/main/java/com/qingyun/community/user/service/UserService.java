package com.qingyun.community.user.service;

import com.qingyun.community.user.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-03
 */
public interface UserService extends IService<User> {
    /**
     * 根据用户id获取用户的完整信息
     * @param userId
     * @return
     */
    User getUserById(Integer userId);

    /**
     * 注册
     * @param user
     * @return
     */
    Map<String, String> register(User user);

    /**
     * 激活
     * @param userId
     * @param code
     * @return 状态码
     */
    int activation(int userId,String code);

    /**
     * 登录
     * @param email 邮箱
     * @param password 密码
     * @param expiredSeconds 登陆凭证的过期时间
     * @return
     */
    Map<String, String> login(String email, String password, int expiredSeconds, HttpSession session);

    /**
     * 修改当前登录用户的头像
     */
    void updateHeader(String newHeader, HttpSession session);
}
