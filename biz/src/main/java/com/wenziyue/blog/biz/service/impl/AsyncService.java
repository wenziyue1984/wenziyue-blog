package com.wenziyue.blog.biz.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Slf4j
@Service
public class AsyncService {

    @Async("asyncExecutor")
    public void asyncMethod() {
        log.info("当前线程名：{}", Thread.currentThread().getName());
        log.info("异步方法执行中 - TraceId 应该存在");
    }


}
