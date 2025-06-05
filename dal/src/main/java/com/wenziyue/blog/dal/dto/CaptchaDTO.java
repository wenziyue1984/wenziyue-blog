package com.wenziyue.blog.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@SuperBuilder
public class CaptchaDTO implements Serializable {

    private static final long serialVersionUID = 8841300916815562094L;

    @Schema(description = "验证码,base64图片")
    private String captcha;

    @Schema(description = "UUID")
    private String uuid;
}
