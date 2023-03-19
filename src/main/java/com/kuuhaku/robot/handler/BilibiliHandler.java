package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.service.BilibiliService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author by kuuhaku
 * @date 2022/5/23 21:12
 * @description
 */
@HandlerComponent
@Slf4j
public class BilibiliHandler {
    @Autowired
    private BilibiliService bilibiliService;

    @Permission
    @Handler(values = {"订阅"}, types = {HandlerMatchType.START}, description = "群里订阅bilibili某用户，使用格式[订阅+空格+uid]")
    public void subscription(ChannelContext ctx) {
        if (ctx.command().isEmpty()) {
            return;
        }
        String uid = ctx.command().params().get(0);
        if (!NumberUtils.isNumber(uid)) {
            return;
        }
        String name = bilibiliService.addPushGroup(ctx.groupId(), NumberUtils.toLong(uid), true);
        if (name == null) {
            ctx.group().sendMessage("订阅失败");
            return;
        }
        ctx.group().sendMessage("订阅[" + name + "]成功");
    }

    @Permission
    @Handler(values = {"取消订阅"}, types = {HandlerMatchType.START}, description = "群里取消订阅bilibili某用户，使用格式[取消订阅+空格+uid]")
    public void cancelSubscription(ChannelContext ctx) {
        if (ctx.command().isEmpty()) {
            return;
        }
        String uid = ctx.command().params().get(0);
        if (!NumberUtils.isNumber(uid)) {
            return;
        }
        MessageChain result = MessageUtils.newChain();
        bilibiliService.removePushGroup(ctx.groupId(), NumberUtils.toLong(uid));
        ctx.group().sendMessage("取消订阅成功");
    }

    @Permission
    @Handler(values = {"订阅列表"}, types = {HandlerMatchType.COMPLETE}, description = "群里当前订阅列表")
    public void NowSubscriptionList(ChannelContext ctx) {
        List<String> list = bilibiliService.subscriptionList(ctx.groupId());
        if (list.size() == 0) {
            ctx.group().sendMessage("当前没有任何订阅");
            return;
        }
        String result = Strings.join(list, '\n');
        ctx.group().sendMessage("当前订阅列表\n" + result);
    }
}
