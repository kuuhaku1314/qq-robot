package com.kuuhaku.robot.event;

import com.kuuhaku.robot.handler.unity.NudgeHandler;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.NudgeEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author by kuuhaku
 * @Date 2021/2/12 23:57
 * @Description 戳一戳监听器
 */
@Component
@Slf4j
public class NudgeEvents extends SimpleListenerHost {
    @Autowired
    private NudgeHandler nudgeHandler;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull NudgeEvent event) throws Exception {
        nudgeHandler.toNudge(event);
        return ListeningStatus.LISTENING;
    }

}
