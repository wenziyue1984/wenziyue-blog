package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.ChatSessionEntity;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.mapper.ChatSessionMapper;
import com.wenziyue.blog.dal.service.ChatSessionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author wenziyue
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSessionEntity> implements ChatSessionService {
    @Override
    public List<UserEntity> getUserInfo(Long id, List<Long> sessionIdList) {
        if (id == null || sessionIdList == null || sessionIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.getUserInfo(id, sessionIdList);
    }
}
