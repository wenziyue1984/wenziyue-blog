package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@Builder
public class TagDTO implements Serializable {

    private static final long serialVersionUID = 5531267881418327227L;

    @Schema(description = "标签id", example = "1", requiredMode = Schema.RequiredMode.AUTO)
    private Long id;

    @Schema(description = "标签名称", example = "name", requiredMode = Schema.RequiredMode.AUTO)
    @Size(max = 20, min = 1, message = "标签长度应在 1~20 个字符之间")
    private String name;

    @Schema(description = "标签状态 0启用 1禁用", example = "0", requiredMode = Schema.RequiredMode.AUTO)
    private Integer status;

}
