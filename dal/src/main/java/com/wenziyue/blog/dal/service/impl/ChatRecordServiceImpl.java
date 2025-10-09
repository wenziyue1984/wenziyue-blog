package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import com.wenziyue.blog.dal.mapper.ChatRecordMapper;
import com.wenziyue.blog.dal.service.ChatRecordService;
import com.wenziyue.blog.dal.dto.SessionUnreadDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author wenziyue
 */
@Service
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecordEntity> implements ChatRecordService {
    @Override
    public Long getSessionLastSeq(Long smallUserId, Long bigUserId) {
        if (smallUserId == null || bigUserId == null) {
            return null;
        }
        return baseMapper.getSessionLastSeq(smallUserId, bigUserId);
    }

    @Override
    public List<ChatRecordEntity> getUnreadMsgForSession(Long userId, List<Long> sessionIdList, int num) {
        if (userId == null || sessionIdList == null || sessionIdList.isEmpty() || num < 1) {
            return Collections.emptyList();
        }
        return baseMapper.getUnreadMsgForSession(userId, sessionIdList, num);
    }

    @Override
    public List<SessionUnreadDTO> getSessionUnreadCount(Long userId, List<Long> sessionIdList) {
        if (userId == null || sessionIdList == null || sessionIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.getSessionUnreadCount(userId, sessionIdList);
    }

    @Override
    public List<ChatRecordEntity> getLastMsgForSession(List<Long> sessionIdList) {
        if (sessionIdList == null || sessionIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.getLastMsgForSession(sessionIdList);
    }
}
