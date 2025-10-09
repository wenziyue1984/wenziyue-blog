package com.wenziyue.blog.dal.service;

import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import com.wenziyue.blog.dal.dto.SessionUnreadDTO;
import com.wenziyue.mybatisplus.base.PageExtendService;

import java.util.List;

/**
 * @author wenziyue
 */
public interface ChatRecordService extends PageExtendService<ChatRecordEntity> {

    Long getSessionLastSeq(Long smallUserId, Long bigUserId);

    /**
     * 获取sessionIdList中会话的前num条未读消息，按照消息发送时间的正序
     * @param sessionIdList 对话id列表
     * @param num 数量
     * @return 消息列表
     */
    List<ChatRecordEntity> getUnreadMsgForSession(Long userId, List<Long> sessionIdList, int num);

    /**
     * 获取sessionIdList中会话的未读消息数
     * @param sessionIdList 对话id列表
     * @return 未读消息数列表
     */
    List<SessionUnreadDTO> getSessionUnreadCount(Long userId, List<Long> sessionIdList);

    /**
     * 获取sessionIdList中会话的最后一条消息
     * @param sessionIdList 对话id列表
     * @return 最后一条消息列表
     */
    List<ChatRecordEntity> getLastMsgForSession(List<Long> sessionIdList);
}
