package com.qingyun.community.user.service;

import com.qingyun.community.user.pojo.LoginTicket;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-04
 */
public interface LoginTicketService extends IService<LoginTicket> {
    /**
     * 插入一条登录凭证
     * @param loginTicket
     */
    void insertOne(LoginTicket loginTicket);

    /**
     * 根据ticket凭证查询LoginTicket
     * @param ticket
     * @return
     */
    LoginTicket selectByTicket(String ticket);
}
