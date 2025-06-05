package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户注册dto
 *
 * @author wenziyue
 */
@Data
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 5578026044099068673L;

    @Schema(description = "用户名", example = "name", maxLength = 20, minLength = 2, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 2, max = 20, message = "用户名长度应在 2~20 个字符之间")
    @NotBlank(message = "用户名不能为空")
    private String name;

    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "邮箱", example = "example@xxx.com", maxLength = 20)
    @Email(message = "邮箱格式不正确")
    @Size(max = 20, message = "邮箱最长 20 个字符")
    private String email;

    @Schema(description = "手机", example = "13900000000", maxLength = 20, minLength = 6)
    @Size(max = 20, min = 6, message = "手机最长 20 个字符，最短 6 个字符")
    private String phone;

    @Schema(description = "个人简介", example = "个人简介", maxLength = 255)
    @Size(max = 255, message = "个人简介最长 255 个字符")
    private String bio;
}
