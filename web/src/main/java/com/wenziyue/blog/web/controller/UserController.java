package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.dao.UserPageDTO;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.biz.service.impl.AsyncService;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.framework.trace.MdcExecutors;
import com.wenziyue.framework.trace.MdcTaskDecorator;
import com.wenziyue.mybatisplus.page.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * @author wenziyue
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
public class UserController {

    private final BizUserService bizUserService;
    private final AsyncService asyncService;
    private final MdcTaskDecorator mdcTaskDecorator;

    @GetMapping("/{id}")
    public UserEntity testUser(@PathVariable("id") Long id) {
        return bizUserService.queryUserById(id);
    }

    @GetMapping("/page")
    public PageResult<UserEntity> listProducts(UserPageDTO dto) {
        log.info("dto:{}", dto);
        return bizUserService.pageUser(dto);
    }

    @GetMapping("/uid")
    public Long testUid() {
        return bizUserService.testUid();
    }

    @GetMapping(value = "/testasync")
    public String testAsync() {
        asyncService.asyncMethod();
        return "触发 @Async 方法";
    }

    @GetMapping("/testfuture")
    public String testCompletableFuture() {
        log.info("主线程启动任务");

        ExecutorService executor = MdcExecutors.newFixedThreadPoolWithMdc(2, mdcTaskDecorator);

        CompletableFuture.runAsync(() -> {
            log.info("异步任务 A 执行中...");
        }, executor);

        CompletableFuture.runAsync(() -> {
            log.info("异步任务 B 执行中...");
        }, executor);

        return "CompletableFuture 异步任务已提交";
    }

}
