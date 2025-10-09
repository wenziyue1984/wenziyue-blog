package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.service.BizChatService;
import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import com.wenziyue.mybatisplus.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

/**
 * @author wenziyue
 */
@RestController("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final BizChatService bizChatService;

    @MessageMapping("/chat.send")
    public void send(ChatMsgDTO msg, Principal me) {
        bizChatService.send(msg, me);
    }

    @Operation(summary = "分页获取聊天记录", description = "获取聊天记录")
    @PostMapping("/getChatRecordsPage")
    public PageResult<ChatRecordEntity> getChatRecordsPage(@Parameter(description = "分页参数", required = true) @Valid @RequestBody ChatRecordPageDTO dto) {
        return bizChatService.getChatRecordsPage(dto);
    }

    @Operation(summary = "获取会话id", description = "获取会话id")
    @GetMapping("/getSessionId/{toUserId}")
    public Long getSessionId(@Parameter(description = "对方用户id", required = true) @PathVariable Long toUserId) {
        return bizChatService.getSessionId(toUserId);
    }


    @Operation(summary = "消息已读", description = "消息已读")
    @PostMapping("/readed/{sessionId}/{seq}")
    public void read(@Parameter(description = "消息已读参数", required = true) @PathVariable Long sessionId, @PathVariable Long seq) {
        bizChatService.read(sessionId, seq);
    }

    @Operation(summary = "获取未读消息数", description = "获取未读消息数")
    @GetMapping("/getUnreadCount/{sessionId}")
    public Long getUnreadCount(@Parameter(description = "会话id", required = true) @PathVariable Long sessionId) {
        return bizChatService.getUnreadCount(sessionId);
    }

    @Operation(summary = "获取未读消息数", description = "获取未读消息数")
    @PostMapping("/getUnreadCount/{sessionId}")
    public Map<Long, Long> getUnreadCount(@Parameter(description = "未读消息数参数", required = true) @Valid @RequestBody ChatUnreadCountDTO dto) {
        return bizChatService.getUnreadCount(dto);
    }

    @Operation(summary = "聊天置顶", description = "聊天置顶")
    @GetMapping("/setTopChat/{sessionId}")
    public void setTopChat(@Parameter(description = "会话id", required = true) @PathVariable Long sessionId) {
        bizChatService.setTopChat(sessionId);
    }

    @Operation(summary = "取消聊天置顶", description = "取消聊天置顶")
    @GetMapping("/cancelTopChat/{sessionId}")
    public void cancelTopChat(@Parameter(description = "会话id", required = true) @PathVariable Long sessionId) {
        bizChatService.cancelTopChat(sessionId);
    }

    @Operation(summary = "聊天免打扰", description = "聊天免打扰")
    @GetMapping("/muteChat/{sessionId}")
    public void muteChat(@Parameter(description = "会话id", required = true) @PathVariable Long sessionId) {
        bizChatService.muteChat(sessionId);
    }

    @Operation(summary = "取消聊天免打扰", description = "取消聊天免打扰")
    @GetMapping("/cancelMuteChat/{sessionId}")
    public void cancelMuteChat(@Parameter(description = "会话id", required = true) @PathVariable Long sessionId) {
        bizChatService.cancelMuteChat(sessionId);
    }

    @Operation(summary = "分页获取未读聊天会话", description = "分页获取未读聊天会话")
    @PostMapping("/getUnreadMessagePage")
    public PageResult<ChatUnreadMessageDTO> getUnreadMessagePage(@Parameter(description = "分页参数", required = true) @Valid @RequestBody ChatUnreadRecordPageDTO dto) {
        return bizChatService.getUnreadMessagePage(dto);
    }



}
