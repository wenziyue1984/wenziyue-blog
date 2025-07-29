package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@Builder
public class FavoritesFolderDTO implements Serializable {

    private static final long serialVersionUID = -3805449140710560903L;

    @Schema(description = "文章id", requiredMode = Schema.RequiredMode.AUTO)
    private Long id;

    @Schema(description = "标题", example = "name", maxLength = 20, minLength = 1, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 20, message = "标题长度应在 1~20 个字符之间")
    @NotBlank(message = "标题不能为空")
    private String name;

    @Schema(description = "封面图", example = "coverUrl", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    @Max(value = 255, message = "标题长度应在 1~20 个字符之间")
    private String coverUrl;

    @Schema(description = "权限，0公开，1隐藏", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer auth;
}
