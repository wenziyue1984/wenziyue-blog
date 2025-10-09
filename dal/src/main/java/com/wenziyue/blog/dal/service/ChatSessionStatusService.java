package com.wenziyue.blog.dal.service;

import com.wenziyue.blog.dal.entity.ChatSessionStatusEntity;
import com.wenziyue.mybatisplus.base.PageExtendService;

/**
 * @author wenziyue
 */
public interface ChatSessionStatusService extends PageExtendService<ChatSessionStatusEntity> {
    /**
     * 获取最大置顶值
     * @param userId 用户id
     * @return 最大置顶值，0表示无置顶会话
     */
    int getMaxTopValue(Long userId);
}
