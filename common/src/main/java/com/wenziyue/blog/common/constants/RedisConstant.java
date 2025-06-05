package com.wenziyue.blog.common.constants;

/**
 * @author wenziyue
 */
public interface RedisConstant {

    String ID_GENERATOR_KEY = "blog:id";

    /**
     * 登录时存储用户信息
     */
    String LOGIN_TOKEN_KEY = "blog:login:userDetail:";

    /**
     * 验证码
     */
    String CAPTCHA_KEY = "blog:captcha:";
}
