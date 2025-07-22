package com.wenziyue.blog.web.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author wenziyue
 */
public class DotenvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        Dotenv dotenv = Dotenv.configure()
                .directory("../")   // 根目录 .env
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }
}
