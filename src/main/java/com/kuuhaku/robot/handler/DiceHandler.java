package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.common.constant.PermissionRank;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Dice;

/**
 * @Author by kuuhaku
 * @Date 2021/2/14 3:22
 * @Description 骰子
 */
@HandlerComponent
@Slf4j
public class DiceHandler {

    @Permission(level = PermissionRank.ADMIN)
    @Handler(values = {"骰子"}, types = {HandlerMatchType.START}, description = "可指定骰子点数，如[骰子 5],也可以直接[骰子]")
    public void sendDice(ChannelContext ctx) {
        if (ctx.command().paramSize() != 1) {
            ctx.group().sendMessage(Dice.random());
        } else {
            Dice dice = new Dice(RandomUtil.ParseIntWithRange(ctx.command().params().get(0), 1, 6, 1));
            ctx.group().sendMessage(dice);
        }
    }

}
