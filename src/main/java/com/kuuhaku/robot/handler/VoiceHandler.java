package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.VoiceService;
import com.kuuhaku.robot.service.AudioService;
import com.kuuhaku.robot.service.MusicService;
import net.mamoe.mirai.message.data.MessageChain;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author by kuuhaku
 * @Date 2021/2/15 0:59
 * @Description 发送语音
 */
@HandlerComponent
public class VoiceHandler {

    @Autowired
    private AudioService audioService;

    @Permission
    @Handler(values = {"MoeGoe"}, types = {HandlerMatchType.START}, description = "发送语音")
    public void sendMoeGoe(ChannelContext ctx) {
        MessageChain msg = audioService.ReadText(ctx, ctx.command().getMsg().replaceFirst("MoeGoe", ""), false);
        ctx.group().sendMessage(msg);
    }

    @Permission
    @Handler(values = {"萌音"}, types = {HandlerMatchType.START}, description = "发送语音")
    public void sendMoeGoeChinese(ChannelContext ctx) {
        MessageChain msg = audioService.ReadText(ctx, ctx.command().getMsg().replaceFirst("萌音", ""), true);
        ctx.group().sendMessage(msg);
    }

}
