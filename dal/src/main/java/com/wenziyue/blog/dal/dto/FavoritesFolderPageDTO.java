package com.wenziyue.blog.dal.dto;

import com.wenziyue.mybatisplus.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * @author wenziyue
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FavoritesFolderPageDTO extends PageRequest {

    private static final long serialVersionUID = 6468174814016329613L;

    @Schema(description = "收藏夹名称", example = "name", requiredMode = Schema.RequiredMode.AUTO)
    @Size(max = 20, message = "收藏夹名称长度不能超过 20 个字符")
    private String name;
}
