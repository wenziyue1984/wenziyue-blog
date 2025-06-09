package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class GoogleLoginDTO implements Serializable {

    private static final long serialVersionUID = 1323325600699687307L;

    @Schema(description = "前端拿到的 Google ID Token（JWT）")
    @NotBlank
    private String idToken;
}
