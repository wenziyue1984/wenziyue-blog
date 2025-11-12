package com.wenziyue.blog.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenziyue.blog.dal.dto.UserInfoDTO;
import com.wenziyue.blog.dal.entity.UserFollowEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * @author wenziyue
 */
public interface UserFollowMapper extends BaseMapper<UserFollowEntity> {

    @Delete("delete from TB_WZY_BLOG_USER_FOLLOW where user_id = #{currentUserId} and follow_user_id = #{userId}")
    void physicalDelete(@Param("currentUserId") Long currentUserId, @Param("userId") Long userId);

    IPage<UserInfoDTO> pageFollowUser(Page<?> page, @Param("sort") Integer sort, @Param("name") String name, @Param("userId") Long userId);

    IPage<UserInfoDTO> pageFansUser(Page<?> page, @Param("sort") Integer sort, @Param("name") String name, @Param("userId") Long userId);
}
