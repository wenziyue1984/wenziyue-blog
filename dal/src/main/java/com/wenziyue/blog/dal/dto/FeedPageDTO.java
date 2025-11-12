package com.wenziyue.blog.dal.dto;

import com.wenziyue.mybatisplus.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author wenziyue
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FeedPageDTO extends PageRequest {

    private static final long serialVersionUID = 489184528925403190L;

    @Schema(description = "从什么时间往后查", example = "2025-01-08 10:34:45")
    private LocalDateTime lastTime;
}
