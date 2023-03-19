package com.kuuhaku.robot.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 1:20
 * @Description 执行一些耗时任务的执行器
 */
@Service
public class TaskService {
    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolExecutor executor;

    public void submitTask(Runnable task) {
        executor.execute(task);
    }
}
