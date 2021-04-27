package com.kuuhaku.robot.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 14:23
 * @Description 定时任务线程池
 */
@Configuration
public class ScheduledExecutorConfig {

    public final int core = Runtime.getRuntime().availableProcessors();

    public final ThreadFactory factory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(0);
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("schedule-work-thread---" + count.getAndAdd(1));
            return new Thread(r);
        }
    };

    @Bean("scheduledExecutor")
    public ScheduledThreadPoolExecutor scheduledExecutor() {
        return new ScheduledThreadPoolExecutor(core, factory);
    }
}
