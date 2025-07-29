package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticlePvDTO implements Serializable {

    private static final long serialVersionUID = -8881907547648413359L;

    @Schema(description = "文章id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章id不能为空")
    private String articleId;

    @Schema(description = "token,暂时为Authorization", requiredMode = Schema.RequiredMode.AUTO)
    private String token;

    @Schema(description = "ip", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String ip;

    @Schema(description = "userAgent", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String userAgent;

    @Schema(description = "时间戳", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String timestamp;

    @Schema(description = "referer", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "referer不能为空")
    private String referer;

    @Schema(description = "url", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "url不能为空")
    private String url;
}
