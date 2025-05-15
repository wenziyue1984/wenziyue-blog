package com.wenziyue.blog.infra.async;

import com.wenziyue.framework.trace.MdcTaskDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author wenziyue
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

    private final MdcTaskDecorator mdcTaskDecorator;

    /**
     * 创建异步线程池，使用的时候在方法上加注解：@Async("asyncExecutor")
     *
     * @return executor
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.initialize();
        return executor;
    }
}
