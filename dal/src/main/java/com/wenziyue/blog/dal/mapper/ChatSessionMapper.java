package com.wenziyue.blog.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenziyue.blog.dal.entity.ChatSessionEntity;
import com.wenziyue.blog.dal.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wenziyue
 */
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {

//    @Select("select t1.* from TB_WZY_BLOG_CHAT_SESSION_STATUS t0 " +
//            "left join TB_WZY_BLOG_USER t1 on t0.other_user_id = t1.id and t0.user_id = #{userId} " +
//            "where t0.session_id in #{sessionIdList}")
    List<UserEntity> getUserInfo(@Param("userId") Long userId, @Param("sessionIdList") List<Long> sessionIdList);
}
