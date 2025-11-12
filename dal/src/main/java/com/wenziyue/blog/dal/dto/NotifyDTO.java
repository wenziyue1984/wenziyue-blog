package com.wenziyue.blog.dal.dto;

import com.wenziyue.blog.common.enums.NotifyInboxStatusEnum;
import com.wenziyue.blog.dal.entity.NotifyInboxEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyDTO implements Serializable {

    private static final long serialVersionUID = 4661765128382947178L;

    @Schema(description = "通知id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "是否已读")
    private Boolean read;

    public NotifyDTO(NotifyInboxEntity entity) {
        this.id = entity.getId();
        this.userId = entity.getRecipientUserId();
        switch (entity.getType()) {
            case COMMENT_LIKE:
                this.content = "用户 " + entity.getActorId() + " 点赞了你的评论 " + entity.getRefId();
                break;
            case SYSTEM_NOTIFY:
                this.content = "系统通知";
                break;
            default:
                    this.content = "未知通知";
        }
        this.read = entity.getStatus() == NotifyInboxStatusEnum.READ;
    }
}
