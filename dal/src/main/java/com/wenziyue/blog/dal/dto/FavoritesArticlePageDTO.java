package com.wenziyue.blog.dal.dto;

import com.wenziyue.mybatisplus.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wenziyue
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FavoritesArticlePageDTO extends PageRequest {

    private static final long serialVersionUID = -1263823342736553327L;

    @Schema(description = "收藏夹id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long favoritesFolderId;
}
