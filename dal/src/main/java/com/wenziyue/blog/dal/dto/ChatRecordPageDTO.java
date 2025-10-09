package com.wenziyue.blog.dal.dto;

import com.wenziyue.mybatisplus.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wenziyue
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatRecordPageDTO extends PageRequest {

    private static final long serialVersionUID = 66523196363349973L;

    @Schema(description = "会话id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "对方用户id", requiredMode = Schema.RequiredMode.AUTO)
    private Long toUserId;

}
