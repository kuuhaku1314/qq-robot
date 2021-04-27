package com.kuuhaku.robot.event;

import com.kuuhaku.robot.core.service.CommandService;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * @Author   by kuuhaku
 * @Date     2021/2/10 20:18
 * @Description 消息监听器
 */
@Component
@Slf4j
public class MessageEvents extends SimpleListenerHost {
    @Autowired
    private CommandService commandService;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MessageEvent event) throws Exception {
        commandService.callCommandHandler(event);
        return ListeningStatus.LISTENING;
    }
}
