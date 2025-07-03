package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author wenziyue
 */
@Data
public class SlugDTO implements Serializable {

    private static final long serialVersionUID = 6737752899075020426L;

    @Schema(description = "标题", example = "name", maxLength = 20, minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 20, message = "标题长度应在 1~20 个字符之间")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容", example = "content", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, message = "文章内容长度应大于1个字符")
    @NotBlank(message = "内容不能为空")
    private String content;

    @Schema(description = "摘要", example = "summary", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "摘要长度应小于100个字符")
    private String summary;

    @Schema(description = "listenKey", example = "用于监听redis的key，前端忽略", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String listenKey;

    @Schema(description = "已使用的slug", example = "已使用的slug，前端忽略", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> usedSlugs;

    @Schema(description = "文章id", example = "文章id，前端忽略", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long articleId;
}
