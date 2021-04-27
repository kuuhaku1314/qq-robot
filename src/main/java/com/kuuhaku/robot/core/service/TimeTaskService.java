package com.kuuhaku.robot.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 17:04
 * @Description 定时任务执行服务
 */
@Service
@Slf4j
public class TimeTaskService {

    @Autowired
    @Qualifier("scheduledExecutor")
    private ScheduledThreadPoolExecutor executor;

    private final Map<String, ScheduledFuture<?>> futureMap = new ConcurrentHashMap<>();

    public final int maxSize = 1000;

    public synchronized void submitTask(Runnable task, String key, long delay, TimeUnit timeUnit) {
        checkMap(futureMap);
        ScheduledFuture<?> future = executor.schedule(task, delay, timeUnit);
        futureMap.put(key, future);
    }

    public boolean cancelTask(String key) {
        ScheduledFuture<?> scheduledFuture = futureMap.get(key);
        if (scheduledFuture == null) {
            return false;
        }
        scheduledFuture.cancel(false);
        futureMap.remove(key);
        return true;
    }

    /**
     * 超过一千条清除
     * @param map 任务结果map
     */
    private synchronized void checkMap(Map<String, ScheduledFuture<?>> map) {
        if (map.size() > maxSize) {
            Set<Map.Entry<String, ScheduledFuture<?>>> entries = map.entrySet();
            for (Map.Entry<String, ScheduledFuture<?>> entry : entries) {
                if (entry.getValue().isDone() || entry.getValue().isCancelled()) {
                    map.remove(entry.getKey());
                }
            }
            log.info("消息清理完成");
        }
    }
}
