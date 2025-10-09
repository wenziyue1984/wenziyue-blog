package com.wenziyue.blog.dal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRecordsSaveMqDTO implements Serializable {

    private static final long serialVersionUID = 3974440873265389205L;

    private Long fromUserId;
    private Long toUserId;
    private String content;
    private Long timestamp;// 幂等键
    private Long seq;

    public ChatRecordsSaveMqDTO(ChatMsgDTO msg, Long fromUserId, Long seq) {
        this.toUserId = msg.getToUserId();
        this.content = msg.getContent();
        this.timestamp = msg.getTimestamp();
        this.fromUserId = fromUserId;
        this.seq = seq;
    }
}
