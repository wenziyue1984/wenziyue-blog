package com.wenziyue.blog.biz.dto;

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
    private String verificationCode;
}
