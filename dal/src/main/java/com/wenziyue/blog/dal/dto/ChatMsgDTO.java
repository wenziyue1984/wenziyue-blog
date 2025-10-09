package com.wenziyue.blog.dal.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class ChatMsgDTO implements Serializable {

    private static final long serialVersionUID = 5690723137824638275L;

    private Long toUserId;
    private String content;
    private Long timestamp;// 幂等键，可以用毫秒级时间戳
}
