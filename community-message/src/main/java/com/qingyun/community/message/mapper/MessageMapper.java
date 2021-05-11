package com.qingyun.community.message.mapper;

import com.qingyun.community.message.pojo.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 張青云
 * @since 2021-05-09
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("select * from message where id in (select max(id) from message where status != 2 \n" +
            "        and from_id != 1\n" +
            "        and (from_id = #{userId} or to_id = #{userId})\n" +
            "        group by conversation_id) " +
            "        order by id desc" +
            "        limit #{offset}, #{limit}")
    //  子查询查出来的是按会话分组后每个会话的最后一条消息的id
    List<Message> getMessagesByUserId(Integer userId, Integer offset, Integer limit);


    @Select("select count(m.maxid) from (\n" +
            "            select max(id) as maxid from message\n" +
            "            where status != 2\n" +
            "            and from_id != 1\n" +
            "            and (from_id = #{userId} or to_id = #{userId})\n" +
            "            group by conversation_id) as m")
    int getConversationCount(Integer userId);


    @Select("select * from message\n" +
            "        where status != 2\n" +
            "        and from_id != 1\n" +
            "        and conversation_id = #{conversationId}\n" +
            "        order by id desc\n" +
            "        limit #{offset}, #{limit}")
    List<Message> getLetters(String conversationId, Integer offset, Integer limit);

    @Select("select count(id) from message\n" +
            "        where status != 2\n" +
            "        and from_id != 1\n" +
            "        and conversation_id = #{conversationId}")
    int getConversationCountById(String conversationId);
}
