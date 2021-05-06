package com.qingyun.community.user;

import com.qingyun.community.user.third.MailClient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.MessagingException;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserApplication.class})
class UserApplicationTests {

    @Autowired
    private MailClient mailClient;

    @Test
    void contextLoads() {
        try {
            mailClient.sendMail("2216094996@qq.com", "test", "hello");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
