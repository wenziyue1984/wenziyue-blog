package com.wenziyue.blog.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import com.wenziyue.blog.dal.dto.SessionUnreadDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wenziyue
 */
public interface ChatRecordMapper extends BaseMapper<ChatRecordEntity>{

    @Select("SELECT t0.seq " +
            "FROM TB_WZY_BLOG_CHAT_RECORD t0 left join TB_WZY_BLOG_CHAT_SESSION t1 on t0.session_id = t1.id " +
            "WHERE t1.small_user_id = #{smallUserId} and t1.big_user_id = #{bigUserId} ORDER BY seq DESC LIMIT 1")
    Long getSessionLastSeq(@Param("smallUserId") Long smallUserId, @Param("bigUserId") Long bigUserId);

    List<ChatRecordEntity> getUnreadMsgForSession(@Param("userId") Long userId
            , @Param("sessionIdList") List<Long> sessionIdList, @Param("num") int num);

    List<SessionUnreadDTO> getSessionUnreadCount(@Param("userId") Long userId, @Param("sessionIdList") List<Long> sessionIdList);

    List<ChatRecordEntity> getLastMsgForSession(List<Long> sessionIdList);
}
