package com.qingyun.community.message.service;

import com.qingyun.community.message.pojo.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 張青云
 * @since 2021-05-09
 */
public interface MessageService extends IService<Message> {

    /**
     * 查询某个用户的会话列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> getMessagesByUserId(Integer userId, Integer offset, Integer limit);

    /**
     * 查询当前用户的会话数量
     * @param userId 当前登录用户
     * @return
     */
    int getConversationCount(Integer userId);

    /**
     * 查询某个会话的私信信息
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> getLetters(String conversationId, Integer offset, Integer limit);

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId
     * @return
     */
    int getConversationCountById(String conversationId);

    /**
     * 查询未读私信的数量
     * @param conversationId 会话id，为null则查询该用户全部的未读私信数量
     * @return
     */
    int getUnReadLettersCount(Integer userId, String conversationId);

    /**
     * 增加一条私信
     * @param message
     * @return
     */
    int addMessage(Message message);

    /**
     * 修改Message的状态
     * @param ids
     * @param status
     */
    void updateMessageStatus(List<Integer> ids, int status);

    /**
     * 查询某个topic下最新的一条消息
     * @param userId
     * @param topic
     * @return
     */
    Message getLatestNotice(int userId, String topic);

    /**
     * 查询某个主题所包含的通知数量
     * @param userId
     * @param topic
     * @return
     */
    int getNoticeCount(int userId, String topic);

    /**
     * 查询未读的通知的数量
     * @param userId
     * @param topic  如果topic为null，则查询全部主题
     * @return
     */
    int getNoticeUnreadCount(int userId, String topic);

    /**
     *  查询某个topic的全部消息
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return
     */
    List<Message> getNotices(int userId, String topic, int offset, int limit);

    /**
     * 查询未读消息的数量，包括私信、系统消息
     * @param userId
     * @return
     */
    int getUnreadCount(int userId);
}
