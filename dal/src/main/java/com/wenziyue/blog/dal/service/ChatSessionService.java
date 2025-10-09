package com.wenziyue.blog.dal.service;

import com.wenziyue.blog.dal.entity.ChatSessionEntity;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.mybatisplus.base.PageExtendService;

import java.util.List;

/**
 * @author wenziyue
 */
public interface ChatSessionService extends PageExtendService<ChatSessionEntity> {
    List<UserEntity> getUserInfo(Long id, List<Long> sessionIdList);
}
