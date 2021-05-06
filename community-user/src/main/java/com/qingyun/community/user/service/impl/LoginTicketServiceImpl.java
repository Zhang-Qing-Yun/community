package com.qingyun.community.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qingyun.community.user.pojo.LoginTicket;
import com.qingyun.community.user.mapper.LoginTicketMapper;
import com.qingyun.community.user.service.LoginTicketService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-04
 */
@Service
public class LoginTicketServiceImpl extends ServiceImpl<LoginTicketMapper, LoginTicket> implements LoginTicketService {

    @Override
    public void insertOne(LoginTicket loginTicket) {
        baseMapper.insert(loginTicket);
    }

    @Override
    public LoginTicket selectByTicket(String ticket) {
        QueryWrapper<LoginTicket> wrapper = new QueryWrapper<>();
        wrapper.eq("ticket", ticket);
        List<LoginTicket> loginTickets = baseMapper.selectList(wrapper);
        if(loginTickets.size() == 1) {
            return loginTickets.get(0);
        }
        return null;
    }
}
