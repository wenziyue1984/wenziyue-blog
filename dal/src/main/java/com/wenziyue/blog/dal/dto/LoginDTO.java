package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = -3667998711777532029L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "username")
    private String name;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "0000")
    private String captchaCode;

    /**
     * 验证码uuid
     */
    @Schema(description = "验证码uuid", example = "80fb0f05-e32b-452c-9713-6b7a5047d792")
    private String captchaUuid;

    /**
     * google登录code
     */
    @Schema(description = "google登录code", example = "0000")
    private String googleCode;

    /**
     * google登录state
     */
    @Schema(description = "google登录state", example = "xyz")
    private String googleState;
}
