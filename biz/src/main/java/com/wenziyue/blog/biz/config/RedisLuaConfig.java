package com.wenziyue.blog.biz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author wenziyue
 */
@Configuration
public class RedisLuaConfig {
    @Bean
    public RedisScript<Long> clearAllTokensScript() {
        return RedisScript.of(
                new ClassPathResource("scripts/clearAllUserTokens.lua"),
                Long.class
        );
    }

    @Bean("likeArticleScript")
    public RedisScript<Boolean> likeArticleScript() {
        return RedisScript.of(
                new ClassPathResource("scripts/likeArticle.lua"),
                Boolean.class
        );
    }

    @Bean("cancelLikeArticleScript")
    public RedisScript<Boolean> cancelLikeArticleScript() {
        return RedisScript.of(new ClassPathResource("scripts/cancelLikeArticle.lua"), Boolean.class);
    }

}
