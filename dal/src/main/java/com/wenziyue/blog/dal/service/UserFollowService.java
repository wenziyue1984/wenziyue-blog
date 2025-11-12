package com.wenziyue.blog.dal.service;

import com.wenziyue.blog.dal.dto.FansUserPageDTO;
import com.wenziyue.blog.dal.dto.FollowUserPageDTO;
import com.wenziyue.blog.dal.dto.UserInfoDTO;
import com.wenziyue.blog.dal.entity.UserFollowEntity;
import com.wenziyue.mybatisplus.base.PageExtendService;
import com.wenziyue.mybatisplus.page.PageResult;

/**
 * @author wenziyue
 */
public interface UserFollowService extends PageExtendService<UserFollowEntity> {

    void physicalDelete(Long currentUserId, Long userId);

    PageResult<UserInfoDTO> pageFollowUser(FollowUserPageDTO dto, Long userId);

    PageResult<UserInfoDTO> pageFansUser(FansUserPageDTO dto, Long userId);
}
