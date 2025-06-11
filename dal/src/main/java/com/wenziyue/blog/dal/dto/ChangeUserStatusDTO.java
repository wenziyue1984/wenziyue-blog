package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class ChangeUserStatusDTO implements Serializable {

    private static final long serialVersionUID = -5873586685908770667L;

    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long id;

    @Schema(description = "用户状态,0正常,1禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer status;
}
