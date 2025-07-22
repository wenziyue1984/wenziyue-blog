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
public class ArticlePageDTO extends PageRequest {

    private static final long serialVersionUID = -2848791221446130063L;

    @Schema(description = "用户id", example = "1")
    private Long userId;

    @Schema(description = "文章id", example = "1")
    private Long articleId;

    @Schema(description = "文章标题", example = "title")
    private String title;

    @Schema(description = "文章标签", example = "tag")
    private String tagId;

    @Schema(description = "过滤隐藏文章", hidden = true)
    private boolean filterHidden;
}
