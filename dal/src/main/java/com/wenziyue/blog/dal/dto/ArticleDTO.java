package com.wenziyue.blog.dal.dto;

import com.wenziyue.blog.common.enums.ArticleStatusEnum;
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
public class ArticleDTO implements Serializable {

    private static final long serialVersionUID = 1220720785369800323L;

    @Schema(description = "文章id", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "标题", example = "name", maxLength = 20, minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 20, message = "标题长度应在 1~20 个字符之间")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容", example = "content", minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, message = "文章内容长度应大于1个字符")
    @NotBlank(message = "内容不能为空")
    private String content;

    @Schema(description = "摘要", example = "summary", maxLength = 100, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "摘要长度应小于100个字符")
    private String summary;

    @Schema(description = "封面图片链接", example = "http://coverurl.com", minLength = 1, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 255, message = "图片链接长度应大于1个字符")
    @NotBlank(message = "封面图片链接不能为空")
    private String coverUrl;

    @Schema(description = "标签列表", example = "['文学','编程']", requiredMode = Schema.RequiredMode.AUTO)
    private List<String> tagList;

    @Schema(description = "阅读量", example = "100", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer viewCount;

    @Schema(description = "点赞数", example = "100", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer likeCount;

    @Schema(description = "slug", example = "slug", maxLength = 255, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String slug;

    @Schema(description = "关键字", example = "keywords", maxLength = 255, requiredMode = Schema.RequiredMode.AUTO)
    private String keywords;

    @Schema(description = "是否置顶", example = "ture", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isTop;

    @Schema(description = "排序序号", example = "1", requiredMode = Schema.RequiredMode.AUTO)
    private Integer sort;

    @Schema(description = "文章状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ArticleStatusEnum status;
}
