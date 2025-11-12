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
public class NotifyPageDTO extends PageRequest {

    private static final long serialVersionUID = -8774586927358252378L;

    @Schema(description = "是否已读", example = "0未读 1已读")
    private Integer read;


}
