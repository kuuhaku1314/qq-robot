package com.kuuhaku.robot.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 1:10
 * @Description 普通任务执行器
 */
@Configuration
public class TaskExecutorConfig {
    public final int corePoolSize = Runtime.getRuntime().availableProcessors();

    public final int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 2;

    public final long keepAlive = 60;

    public final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(1000);

    public final ThreadFactory factory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("work-thread---" + count.getAndAdd(1));
            return new Thread(r);
        }
    };

    @Bean("taskExecutor")
    public ThreadPoolExecutor getExecutor() {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAlive, TimeUnit.SECONDS, queue, factory);
    }

}
