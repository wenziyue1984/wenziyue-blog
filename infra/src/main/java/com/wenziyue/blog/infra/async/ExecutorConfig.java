package com.wenziyue.blog.infra.async;

import com.wenziyue.framework.trace.MdcExecutors;
import com.wenziyue.framework.trace.MdcTaskDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

/**
 * @author wenziyue
 */
@Configuration
@RequiredArgsConstructor
public class ExecutorConfig {

    private final MdcTaskDecorator mdcTaskDecorator;

    @Bean(name = "executorService")
    public ExecutorService executorService() {
        return MdcExecutors.newFixedThreadPoolWithMdc(10, mdcTaskDecorator);
    }
}
