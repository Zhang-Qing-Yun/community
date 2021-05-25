package com.qingyun.community.user.mapper;

import com.qingyun.community.user.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 張青云
 * @since 2021-05-03
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据id列表批量获取数据，
     * 使用union all+动态拼接SQL来完成，避免了使用in会去重的问题
     *
     * @param ids
     * @return
     */
    @Select("<script>" +
            "<foreach collection='ids' item='id' index='i'>" +
            "select * from user " +
            " where id = #{id} " +
            " <if test='i != ids.size-1'>" +
            " union all " +
            " </if> " +
            " </foreach>" +
            "</script>")
    List<User> getUsersByIds(@Param("ids") List<Integer> ids);

}
