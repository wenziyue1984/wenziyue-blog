package com.wenziyue.blog.dal.dto;

import com.wenziyue.blog.common.enums.ChatMsgTypeEnum;
import com.wenziyue.blog.common.enums.ChatRecordStatusEnum;
import com.wenziyue.blog.common.enums.ChatTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wenziyue
 */
@Data
public class ChatRecordDTO implements Serializable {

    private static final long serialVersionUID = 3731784224453970487L;

    @Schema(description = "会话id", example = "1")
    private Long sessionId;

    @Schema(description = "消息序号", example = "1")
    private Long seq;

    @Schema(description = "聊天类型")
    private ChatTypeEnum chatType;

    @Schema(description = "消息类型")
    private ChatMsgTypeEnum msgType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "发送者id")
    private Long fromUserId;

    @Schema(description = "接收者id")
    private Long toUserId;

    @Schema(description = "消息状态")
    private ChatRecordStatusEnum status;

    @Schema(description = "消息已读状态")
    private Boolean readStatus;

    @Schema(description = "消息发送时间")
    private LocalDateTime createTime;
}
