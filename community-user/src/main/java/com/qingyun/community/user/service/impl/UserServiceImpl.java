package com.qingyun.community.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.user.pojo.User;
import com.qingyun.community.user.mapper.UserMapper;
import com.qingyun.community.user.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyun.community.user.third.MailClient;
import com.qingyun.community.user.utils.Constant;
import com.qingyun.community.user.utils.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, Constant {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HostHolder hostHolder;

    // 该服务的地址
    @Value("${community.user.path.domain}")
    private String domain;

    // 发送邮件的api地址
    private String contextPath = "/user/activation";



    @Override
    public User getUserById(Integer userId) {
        User user = baseMapper.selectById(userId);
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = baseMapper.selectOne(wrapper);
        return user;
    }

    @Override
    public Map<String, String> register(User user) {
        Map<String, String> map = new HashMap<>();

        if (user.getPassword().length() < 8) {
            map.put("passwordMsg", "密码必须大于8个字符");
            return map;
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0) {
            map.put("usernameMsg","该账号已经存在！");
            return map;
        }

        QueryWrapper<User> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("email", user.getEmail());
        Integer count2 = baseMapper.selectCount(wrapper2);
        if(count2 > 0) {
            map.put("emailMsg","该邮箱已经被注册");
            return map;
        }

        // 密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(UserUtils.generateUUID());  // 激活码，用于后期的激活
        // 设置头像
        // TODO：可以改为自定义的默认头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        baseMapper.insert(user);

        //  发送激活邮件
        //  TODO: 发送邮件可以改为异步方式
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:88/user/activation/{userId}/{activationCode}
        String url = domain + contextPath + "/" + user.getId() + "/"+user.getActivationCode();
        context.setVariable("url",url);

        String content = templateEngine.process("/mail/activation", context);
        try{
            mailClient.sendMail(user.getEmail(),"激活账号", content);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("发送邮件异常");
        }
        return map;
    }

    public int activation(int userId,String code){
        User user = baseMapper.selectById(userId);

        if (user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            user.setStatus(1);
            baseMapper.updateById(user);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, String> login(String email, String password, int expiredSeconds, HttpSession session) {
        Map<String, String> map = new HashMap<>();

        // 验证邮箱
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        } else {
            String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern p = Pattern.compile(regEx1);
            Matcher m = p.matcher(email);
            if(!m.matches()){
                map.put("emailMsg", "邮箱格式不正确!");
                return map;
            }
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        User user = baseMapper.selectOne(wrapper);
        if (user == null) {
            map.put("emailMsg", "该邮箱还未注册!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("emailMsg", "该邮箱未激活，请尽快激活!");
            return map;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setUserId(user.getId());
//        loginTicket.setTicket(UserUtils.generateUUID());
//        loginTicket.setStatus(0);
//        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketService.insertOne(loginTicket);
//        map.put("ticket", loginTicket.getTicket());


        //  存到redis当中
        com.qingyun.community.base.pojo.User userOfSession = new com.qingyun.community.base.pojo.User();
        BeanUtils.copyProperties(user, userOfSession);
        session.setAttribute("loginUser", userOfSession);
        map.put("ticket", null);
        return map;
    }

    @Override
    public void updateHeader(String newHeader, HttpSession session) {
        // 修改头像，改数据库
        com.qingyun.community.base.pojo.User user = hostHolder.get();
        user.setHeaderUrl(newHeader);
        User user1 = new User();
        user1.setId(user.getId());
        user1.setHeaderUrl(newHeader);
        baseMapper.updateById(user1);
        // 更新ThreadLocal里存的User对象
        hostHolder.set(user);
        // 更新session里存的User对象
        session.setAttribute("loginUser", user);
    }
}
