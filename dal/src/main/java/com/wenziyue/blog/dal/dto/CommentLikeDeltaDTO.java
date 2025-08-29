package com.wenziyue.blog.dal.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author wenziyue
 */
@Data
@Builder
public class CommentLikeDeltaDTO {

    private Long commentId;
    private Integer delta; // 正负皆可
}
