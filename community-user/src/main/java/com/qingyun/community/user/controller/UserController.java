package com.qingyun.community.user.controller;


import com.google.code.kaptcha.Producer;
import com.qingyun.community.base.annotation.LoginRequired;
import com.qingyun.community.base.utils.HostHolder;
import com.qingyun.community.base.utils.RedisKeyUtils;
import com.qingyun.community.user.feignClient.LikeClient;
import com.qingyun.community.user.pojo.User;
import com.qingyun.community.user.service.UserService;
import com.qingyun.community.user.utils.Constant;
import com.qingyun.community.user.utils.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 張青云
 * @since 2021-05-03
 */
@Controller
@RequestMapping("/user")
public class UserController implements Constant,com.qingyun.community.base.utils.Constant {
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LikeClient likeClient;

    @Autowired
    private HostHolder hostHolder;



    @GetMapping("/getUserById/{userId}")
    @ResponseBody
    public User getUserById(@PathVariable(value = "userId") Integer userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/getUsersByIds")
    @ResponseBody
    public List<User> getUsersByIds(@RequestParam("ids") List<Integer> ids) {
        return userService.getUsersByIds(ids);
    }

    @GetMapping("/getUserByUsername/{username}")
    @ResponseBody
    public User getUserByUsername(@PathVariable(value = "username") String username) {
        return userService.getUserByUsername(username);
    }

    @PostMapping("/registerOne")
    // 在SpringMVC调用该方法时就会将user放到model里（因为user是一个实体类，如果是普通参数不会有这个效果）
    public String register(Model model, @Valid User user, BindingResult result) {
        Map<String, String> map = new HashMap<>();
        //  JSR303校验结果
        if(result.hasErrors()) {
            result.getFieldErrors().forEach((item)->{
                //获取出现错误的属性的名字
                String field = item.getField() + "Msg";
                //出现错误时的提示信息
                String message = item.getDefaultMessage();
                map.put(field, message);
            });
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        } else {
            Map<String, String> resultMsg = userService.register(user);
            if(resultMsg.size() == 0) {
                model.addAttribute("msg","注册成功，我们已经向您发送了一封激活邮件，请尽快激活");  // 页面需要的提示信息
                model.addAttribute("target","/community/post/index");  // target是在operate-result.html页面中需要的跳转路径
                return "/site/operate-result";
            }
            model.addAttribute("usernameMsg",resultMsg.get("usernameMsg"));
            model.addAttribute("passwordMsg",resultMsg.get("passwordMsg"));
            model.addAttribute("emailMsg",resultMsg.get("emailMsg"));
            return "/site/register";
        }
    }

    @GetMapping("/register")
    public String register() {
        return "/site/register";
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了");
            model.addAttribute("target","/user/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","该账号已经被激活");
            model.addAttribute("target","/community/post/index");
        }else{
            model.addAttribute("msg","激活失败，您的激活码不正确");
            model.addAttribute("target","/community/post/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/login")
    public String login() {
        return "/site/login";
    }

    /**
     *  生成验证码，因为接下来需要验证，所以需要将验证码保存起来，这里存到了session
     */
    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response){
        //  生成验证码
        String text = kaptchaProducer.createText();
        //  生成验证码图片
        BufferedImage image = kaptchaProducer.createImage(text);

        //  将验证码存到redis当中
        String uuid = UserUtils.generateUUID();
        String redisKey = RedisKeyUtils.getKaptchaKey(uuid);
        redisTemplate.opsForValue().set(redisKey, text,60, TimeUnit.SECONDS);
        //  将该验证码的唯一标识传给cookie
        Cookie cookie = new Cookie("kaptchaOwner", uuid);
        cookie.setMaxAge(60);  // cookie有效时间是60s
        response.addCookie(cookie);


        //to browser
        response.setContentType("image/png");
        try {
            OutputStream os =response.getOutputStream();
            ImageIO.write(image,"png", os);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("输出图片失败");
        }

    }

    @PostMapping("/api_login")
    public String login(String email, String password, String code, boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 检查验证码
        String kaptcha =null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtils.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, String> map = userService.login(email, password, expiredSeconds, session);
        if (map.containsKey("ticket")) {
            model.addAttribute("msg","登陆成功！");  // 页面需要的提示信息
            model.addAttribute("target","/community/post/index");  // target是在operate-result.html页面中需要的跳转路径
            return "/site/operate-result";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession httpSession, Model model) {
        httpSession.removeAttribute("loginUser");
        model.addAttribute("msg","退出登陆成功，即将跳转到首页！");
        model.addAttribute("target","/community/post/index");
        return "/site/operate-result";
    }

    @GetMapping("/setting")
    @LoginRequired
    public String setting() {
        return "/site/setting2";
    }

    @GetMapping("/updateHeader")
    @LoginRequired
    public String updateHeader(String newHeader, HttpSession session) {
        userService.updateHeader(newHeader, session);
        //  TODO:这里的重定向地址写死为localhost了
        return "redirect:http://localhost:88/community/post/index";
    }

    //  查看个人主页
    @GetMapping(path = "/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.getUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user",user);
        //  收到的赞的个数
        long likeCount = likeClient.getUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        long followeeCount = likeClient.getFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = likeClient.getFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.get() != null) {
            hasFollowed = likeClient.hasFollowed(hostHolder.get().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}

