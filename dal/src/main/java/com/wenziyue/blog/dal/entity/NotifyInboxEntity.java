package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.blog.common.enums.NotifyInboxStatusEnum;
import com.wenziyue.blog.common.enums.NotifyInboxTypeEnum;
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
@TableName("TB_WZY_BLOG_NOTIFY_INBOX")
public class NotifyInboxEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("recipient_user_id")
    private Long recipientUserId;

    @TableField("actor_id")
    private Long actorId;

    @TableField("type")
    private NotifyInboxTypeEnum type;

    @TableField("ref_id")
    private Long refId;

    @TableField("status")
    private NotifyInboxStatusEnum status;
}
