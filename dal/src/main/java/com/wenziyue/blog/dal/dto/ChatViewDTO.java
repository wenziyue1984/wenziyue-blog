package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@AllArgsConstructor
public class ChatViewDTO implements Serializable {

    private static final long serialVersionUID = -1974179630163190506L;

    @Schema(description = "发送者id", example = "1")
    private Long fromUserId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息序号")
    private Long seq;

    @Schema(description = "消息时间")
    private Long timestamp;
}
