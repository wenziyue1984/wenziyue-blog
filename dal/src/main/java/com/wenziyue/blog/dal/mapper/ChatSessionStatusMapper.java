package com.wenziyue.blog.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wenziyue.blog.dal.entity.ChatSessionStatusEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author wenziyue
 */
public interface ChatSessionStatusMapper extends BaseMapper<ChatSessionStatusEntity> {

    @Select("select max(top) from TB_WZY_BLOG_CHAT_SESSION_STATUS where user_id = #{userId}")
    int getMaxTopValue(@Param("userId") Long userId);
}
