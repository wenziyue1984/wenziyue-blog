package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class SessionUnreadDTO implements Serializable {

    private static final long serialVersionUID = 5817986132214912724L;

    @Schema(description = "未读消息数")
    private Long unreadCount;

    @Schema(description = "会话id")
    private Long sessionId;
}
