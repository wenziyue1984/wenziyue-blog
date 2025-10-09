package com.wenziyue.blog.dal.dto;

import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wenziyue
 */
@Data
public class ChatUnreadMessageDTO implements Serializable {

    private static final long serialVersionUID = 8322158676061399699L;

    @Schema(description = "会话id")
    private Long sessionId;

    @Schema(description = "对方用户信息")
    private UserInfoDTO user;

    @Schema(description = "未读消息数")
    private Long unreadCount;

    @Schema(description = "最后一条消息内容")
    private ChatRecordEntity lastRecord;

    /**
     * 未读消息，最多20条
     */
    private List<ChatRecordEntity> records;
}
