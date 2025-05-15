package com.wenziyue.blog.infra.schedule;


import com.wenziyue.framework.trace.MdcTaskDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author wenziyue
 */
@Configuration
@RequiredArgsConstructor
public class ScheduleConfig implements SchedulingConfigurer {

    private final MdcTaskDecorator mdcTaskDecorator;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        // 创建调度线程池（注意不是 ThreadPoolTaskExecutor）
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, runnable -> {
            Runnable decorated = mdcTaskDecorator.decorate(runnable);
            Thread thread = new Thread(decorated);
            thread.setName("schedule-" + thread.getId());
            return thread;
        });
        registrar.setScheduler(scheduler);
    }
}
