package com.kuuhaku.robot.event;

import com.kuuhaku.robot.handler.unity.MemberLeaveHandler;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author by kuuhaku
 * @Date 2021/2/14 17:48
 * @Description 离群事件
 */
@Component
public class MemberLeaveEvents extends SimpleListenerHost {
    @Autowired
    private MemberLeaveHandler memberLeaveHandler;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    @NotNull
    @EventHandler
    public ListeningStatus onMessage(@NotNull MemberLeaveEvent event) throws Exception {
        memberLeaveHandler.doHandler(event);
        return ListeningStatus.LISTENING;
    }

}
