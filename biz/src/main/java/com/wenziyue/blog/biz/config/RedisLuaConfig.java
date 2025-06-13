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

}
