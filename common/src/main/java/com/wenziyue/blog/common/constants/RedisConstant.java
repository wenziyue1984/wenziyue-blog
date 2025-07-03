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
     * 用户所有活跃的token集合
     */
    String USER_TOKENS_KEY = "blog:login:userTokens:";

    /**
     * 验证码
     */
    String CAPTCHA_KEY = "blog:captcha:";

    /**
     * 监听生成的slug
     */
    String SLUG_LISTEN_KEY = "blog:slug:listen:";

    /**
     * 文章版本，防止生成summary和slug的时候文章已被修改，也可验证当前文章有无加入mq队列
     */
    String ARTICLE_VERSION_KEY = "blog:article:version:";
}
