package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.core.chain.ChannelContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 2:53
 * @Description 打印日志
 */
@HandlerComponent
@Slf4j
public class LogHandler {

    @Handler(order = Integer.MIN_VALUE)
    public void toRecord(ChannelContext ctx) {
        String message = ctx.event().getMessage().contentToString();
        log.info("{接收到消息} userId:{},userNick:{},msg:{}", ctx.senderId(), ctx.nickname(), message);
    }
}
