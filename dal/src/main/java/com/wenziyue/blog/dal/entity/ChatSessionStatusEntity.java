package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.blog.common.enums.ChatSessionMuteEnum;
import com.wenziyue.mybatisplus.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author wenziyue
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("TB_WZY_BLOG_CHAT_SESSION_STATUS")
public class ChatSessionStatusEntity extends BaseEntity {

    @TableField("session_id")
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("other_user_id")
    private Long otherUserId;

    @TableField("last_read_seq")
    private Long lastReadSeq;

//    @TableField("clear_before_seq")
//    private Long clearBeforeSeq;

    @TableField("top")
    private Integer top;

    @TableField("mute")
    private ChatSessionMuteEnum mute;
}
