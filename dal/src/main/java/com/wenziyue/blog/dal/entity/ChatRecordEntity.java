package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.blog.common.enums.ChatMsgTypeEnum;
import com.wenziyue.blog.common.enums.ChatTypeEnum;
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
@TableName("TB_WZY_BLOG_CHAT_RECORD")
public class ChatRecordEntity extends BaseEntity {

    @TableField("session_id")
    private Long sessionId;

    @TableField("seq")
    private Long seq;

    @TableField("chat_type")
    private ChatTypeEnum chatType;

    @TableField("msg_type")
    private ChatMsgTypeEnum msgType;

    @TableField("content")
    private String content;

    @TableField("from_user_id")
    private Long fromUserId;

    @TableField("to_user_id")
    private Long toUserId;

//    @TableField("status")
//    private ChatRecordStatusEnum status;

    @TableField("read_status")
    private Boolean readStatus;

    @TableField("timestamp")
    private Long timestamp;

}
