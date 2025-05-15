package com.wenziyue.blog.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wenziyue
 */
@EnableAsync
@EnableScheduling
@MapperScan("com.wenziyue.blog.dal.mapper")
@ComponentScan("com.wenziyue")
@SpringBootApplication(scanBasePackages = {"com.wenziyue"})
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
