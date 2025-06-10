package com.wenziyue.blog.dal.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class UpdatePasswordDTO implements Serializable {

    private static final long serialVersionUID = 6460849931816652343L;

    @NotBlank
    @Size(min = 6, message = "密码最少6个字符")
    private String newPassword;
}
