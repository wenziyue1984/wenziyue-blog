package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.ChatSessionStatusEntity;
import com.wenziyue.blog.dal.mapper.ChatSessionStatusMapper;
import com.wenziyue.blog.dal.service.ChatSessionStatusService;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class ChatSessionStatusServiceImpl extends ServiceImpl<ChatSessionStatusMapper, ChatSessionStatusEntity> implements ChatSessionStatusService {
    @Override
    public int getMaxTopValue(Long userId) {
        return baseMapper.getMaxTopValue(userId);
    }
}
