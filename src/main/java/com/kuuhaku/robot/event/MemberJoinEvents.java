package com.kuuhaku.robot.event;

import com.kuuhaku.robot.handler.unity.MemberJoinHandler;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author by kuuhaku
 * @Date 2021/2/14 17:48
 * @Description 入群事件
 */
@Component
public class MemberJoinEvents extends SimpleListenerHost {
    @Autowired
    private MemberJoinHandler memberJoinHandler;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MemberJoinEvent event) throws Exception {
        memberJoinHandler.doHandler(event);
        return ListeningStatus.LISTENING;
    }

}
