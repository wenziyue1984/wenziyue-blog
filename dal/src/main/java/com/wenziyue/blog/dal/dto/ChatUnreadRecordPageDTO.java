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
public class ChatUnreadRecordPageDTO extends PageRequest {

    private static final long serialVersionUID = 4389155856139065377L;

    @Schema(description = "上一页最后一个对话的时间戳，请求第一页数据的话不传", requiredMode = Schema.RequiredMode.AUTO)
    private Long lastTimeStamp;

    @Schema(description = "上一页最后一个对话的sessionId，请求第一页数据的话不传", requiredMode = Schema.RequiredMode.AUTO)
    private Long lastSessionId;

//    @Schema(description = "查询类型，0查询该时间戳之前的数据，1查询该时间戳之后的数据", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
//    private Integer type;
}
