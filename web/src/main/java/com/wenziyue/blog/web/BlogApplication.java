package com.wenziyue.blog.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author wenziyue
 */
@SpringBootApplication
@MapperScan("com.wenziyue.blog.dal.mapper")
@ComponentScan("com.wenziyue.blog")
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
