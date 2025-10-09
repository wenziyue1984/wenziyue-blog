package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import com.wenziyue.mybatisplus.page.PageResult;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * @author wenziyue
 */
public interface BizChatService {

    void send(ChatMsgDTO msg, Principal me);

    @Transactional
    void read(Long sessionId, Long seq);

    @Transactional(readOnly = true)
    Long getUnreadCount(Long sessionId);

    @Transactional(readOnly = true)
    Map<Long, Long> getUnreadCount(ChatUnreadCountDTO dto);

    @Transactional
    void setTopChat(Long sessionId);

    @Transactional
    void cancelTopChat(Long sessionId);

    @Transactional
    void muteChat(Long sessionId);

    @Transactional
    void cancelMuteChat(Long sessionId);

    @Transactional(readOnly = true)
    PageResult<ChatRecordEntity> getChatRecordsPage(ChatRecordPageDTO dto);

    @Transactional(readOnly = true)
    Long getSessionId(Long toUserId);

    @Transactional(readOnly = true)
    PageResult<ChatUnreadMessageDTO> getUnreadMessagePage(ChatUnreadRecordPageDTO dto);
}