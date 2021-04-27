package com.kuuhaku.robot.event;

import com.kuuhaku.robot.handler.unity.MemberJoinHandler;
import com.kuuhaku.robot.handler.unity.MemberJoinRequestHandler;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/14 17:49
 * @Description 请求入群事件
 */
@Component
public class MemberJoinRequestEvents extends SimpleListenerHost {

    @Autowired
    private MemberJoinRequestHandler memberJoinRequestHandler;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MemberJoinRequestEvent event) throws Exception {
        memberJoinRequestHandler.doHandler(event);
        return ListeningStatus.LISTENING;
    }

}
