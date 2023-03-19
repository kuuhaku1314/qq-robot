package com.kuuhaku.robot.event;

import com.kuuhaku.robot.handler.unity.MessageRecallHandler;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author by kuuhaku
 * @Date 2021/2/12 20:58
 * @Description 消息撤回事件
 */
@Component
@Slf4j
public class MessageRecallEvents extends SimpleListenerHost {
    @Autowired
    private MessageRecallHandler recallHandler;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MessageRecallEvent.GroupRecall event) throws Exception {
        // recallHandler.doHandler(event);
        return ListeningStatus.LISTENING;
    }
}
