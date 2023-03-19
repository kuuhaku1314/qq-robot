package com.kuuhaku.robot.core.service;

import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.chain.ChannelPipeline;
import com.kuuhaku.robot.core.chain.Command;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * @Author by kuuhaku
 * @Date 2021/2/10 17:37
 * @Description 指令相关处理，有需求可以进行改动
 */
@Slf4j
@Service
public class CommandService {
    @Autowired
    private TaskService taskService;

    private ChannelPipeline pipeline;

    /**
     * 注册所有指令
     *
     * @param context app上下文
     */
    public void registerAllCommand(ApplicationContext context) {
        this.pipeline = ChannelPipeline.instance(context);
    }

    /**
     * 通过事件寻找对应handler并调用
     *
     * @param event 事件
     */
    public void callCommandHandler(MessageEvent event) {
        Command command = new Command(event.getMessage().contentToString());
        ChannelContext context = new ChannelContext(command, event);
        taskService.submitTask(() -> pipeline.execute(context));
    }

    public boolean removeCommand(String command) {
        return pipeline.removeCommand(command);
    }

    public boolean restoreCommand(String command) {
        return pipeline.restoreCommand(command);
    }

    public List<String> restoreCommandList() {
        return pipeline.restoreCommandList();
    }

    public Map<String, String> commandMap() {
        return pipeline.commandMap();
    }
}
