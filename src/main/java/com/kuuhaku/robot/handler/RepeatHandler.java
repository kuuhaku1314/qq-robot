package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.service.RepeatService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 3:15
 * @Description 复读机
 */
@HandlerComponent
public class RepeatHandler {
    @Autowired
    private RepeatService repeatService;

    @Handler(order = -10)
    public void repeat(ChannelContext ctx) {
        repeatService.tryRepeat(ctx.event());
    }

}
