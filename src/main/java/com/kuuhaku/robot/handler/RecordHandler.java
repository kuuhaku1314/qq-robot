package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 2:50
 * @Description 保存消息
 */
@HandlerComponent
@Slf4j
public class RecordHandler {
    @Autowired
    private RecordService recordService;

    // 记录群消息，配合防撤回使用
    @Handler(order = -100)
    public void toRecord(ChannelContext ctx) {
        ctx.event().getSource().getIds();
        recordService.record(ctx.groupIdStr(), ctx.event());
    }
}
