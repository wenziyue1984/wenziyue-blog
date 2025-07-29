package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class FavoritesArticleDTO implements Serializable {

    private static final long serialVersionUID = -7345396859283580197L;

    @Schema(description = "收藏夹id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long favoritesFolderId;

    @Schema(description = "文章id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long articleId;
}
