package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.TaskService;
import com.kuuhaku.robot.service.GuessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


@HandlerComponent
@Slf4j
public class GuessHandler {
    @Autowired
    private TaskService taskService;
    @Autowired
    private GuessService guessService;

    @Permission
    @Handler(values = {"灯神"}, types = {HandlerMatchType.COMPLETE}, description = "猜东西")
    public void guessGame(ChannelContext ctx) {
        GuessService.GuessGame guessGame = guessService.newInstance(ctx.group());
        if (guessGame == null) {
            ctx.group().sendMessage("当前正在进行中");
            return;
        }
        Runnable runnable = () -> {
            log.info("此局灯神开始");
            guessGame.start();
            log.info("此局灯神结束");
        };
        taskService.submitTask(runnable);
    }

    @Permission
    @Handler(values = {"Character", "Chinese", "Japanese", "japanese", "English", "character", "chinese", "english", "Y", "N", "DK", "P", "PN", "y", "n", "dk", "p", "pn", "B", "b", "18", "stop"}, types = {HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE}, description = "猜东西指令")
    public void guess(ChannelContext ctx) {
        guessService.sendMsg(ctx.groupId(), ctx.command().baseCommand());
    }
}
