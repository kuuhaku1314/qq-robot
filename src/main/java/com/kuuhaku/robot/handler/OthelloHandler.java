package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.chain.Command;
import com.kuuhaku.robot.entity.othello.OthelloConstant;
import com.kuuhaku.robot.service.OthelloService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author by kuuhaku
 * @Date 2021/2/12 2:04
 * @Description 黑白棋
 */
@HandlerComponent
@Slf4j
public class OthelloHandler {
    @Autowired
    private OthelloService othelloService;
    private final AtomicLong AIId = new AtomicLong(1);

    @Permission
    @Handler(values = {"创建黑白棋"}, types = {HandlerMatchType.COMPLETE}, description = "创建黑白棋局")
    public void createBattle(ChannelContext ctx) {
        int code = othelloService.create(ctx.groupId(), ctx.senderId(), ctx.nickname());
        switch (code) {
            case OthelloService.YOU_IN_BOARD -> {
                ctx.group().sendMessage("你已经在黑白棋对局中了，请勿再次创建");
                return;
            }
            case OthelloService.YOU_CREATED_BOARD -> {
                ctx.group().sendMessage("你已经创建了黑白棋了，找个人加入吧");
                return;
            }
            case OthelloService.OTHERS_CREATED_BOARD -> {
                ctx.group().sendMessage("已经有人创建了黑白棋了，快发送[加入黑白棋]开始对局吧");
                return;
            }
        }
        MessageChain result = MessageUtils.newChain();
        result = result.plus("当前对局所在群为:" + ctx.groupId()).plus("\n");
        result = result.plus("创建对局成功，需要参加此对局请发送[加入黑白棋]。").plus("\n");
        result = result.plus("规则:横坐标为x，纵坐标为y，可以同时进行多盘棋。").plus("\n");
        result = result.plus("x从左往右为1-8，y从上往下为1-8。").plus("\n");
        result = result.plus("如显示有问题请用手机").plus("\n");
        result = result.plus("操作方式为输入指令[00+空格+x+空格+y]。").plus("\n");
        result = result.plus("空格为" + OthelloConstant.BLANK_NAME + "，黑方为" + OthelloConstant.BLACK_NAME + "，白方为" + OthelloConstant.WHITE_NAME).plus("\n");
        result = result.plus("从黑方开始，依次进行,超时时间为30秒，最后棋子多者胜利").plus("\n");
        result = result.plus("发送[黑白棋认输]可以结束已开始对局，发送[取消黑白棋]可以停止创建。");
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"加入黑白棋"}, types = {HandlerMatchType.COMPLETE}, description = "加入黑白棋局")
    public void joinBattle(ChannelContext ctx) {
        int code = othelloService.join(ctx.groupId(), ctx.senderId(), ctx.nickname(), ctx.group(), false);
        switch (code) {
            case OthelloService.YOU_IN_BOARD -> {
                ctx.group().sendMessage("你已经在对局中，请勿再参加了");
            }
            case OthelloService.YOU_CREATED_BOARD -> {
                ctx.group().sendMessage("不能自己和自己对局");
            }
            case OthelloService.OTHERS_CREATED_BOARD -> {
                ctx.group().sendMessage("已经有人创建了黑白棋了，快发送[加入黑白棋]开始对局吧");
            }
            case OthelloService.NOT_FOUND_BOARD -> ctx.group().sendMessage("请先创造黑白棋对局");
        }
    }

    @Permission
    @Handler(values = {"00"}, types = {HandlerMatchType.START}, description = "黑白棋局中用来下棋，格式如[00 1 2]，1为棋的x坐标，2为棋y坐标")
    public void battle(ChannelContext ctx) {
        Command command = ctx.command();
        if (command.paramSize() != 2 || !StringUtils.isNumeric(command.params().get(0)) ||
                !StringUtils.isNumeric(command.params().get(1))) {
            return;
        }
        int code = othelloService.play(ctx.groupId(), ctx.senderId(), Integer.parseInt(command.params().get(0)), Integer.parseInt(command.params().get(1)));
        if (code == OthelloService.NOT_FOUND_BOARD) {
            ctx.group().sendMessage("你没有在对局中");
        }
    }

    @Permission
    @Handler(values = {"黑白棋认输"}, types = {HandlerMatchType.COMPLETE}, description = "黑白棋认输")
    public void give(ChannelContext ctx) {
        int code = othelloService.give(ctx.groupId(), ctx.senderId());
        if (code == OthelloService.NOT_FOUND_BOARD) {
            ctx.group().sendMessage("你没有在对局中");
        }
    }

    @Permission
    @Handler(values = {"取消黑白棋"}, types = {HandlerMatchType.COMPLETE}, description = "无人应战取消创建")
    public void stop(ChannelContext ctx) {
        if (othelloService.remove(ctx.groupId()) == OthelloService.OK) {
            ctx.group().sendMessage("取消创建成功");
        }
    }

    @Permission
    @Handler(values = {"创建AI黑白棋"}, types = {HandlerMatchType.COMPLETE}, description = "开始黑白棋AI对局")
    public void createAIBoard(ChannelContext ctx) {
        int code = othelloService.create(ctx.groupId(), ctx.senderId(), ctx.nickname());
        switch (code) {
            case OthelloService.YOU_IN_BOARD -> {
                ctx.group().sendMessage("你已经在黑白棋对局中了，请勿再次创建");
                return;
            }
            case OthelloService.YOU_CREATED_BOARD -> {
                ctx.group().sendMessage("你已经创建了黑白棋了，找个人加入吧");
                return;
            }
            case OthelloService.OTHERS_CREATED_BOARD -> {
                ctx.group().sendMessage("已经有人创建了黑白棋了，快发送[加入黑白棋]开始对局吧");
                return;
            }
        }
        MessageChain result = MessageUtils.newChain();
        result = result.plus("当前对局所在群为:" + ctx.groupId()).plus("\n");
        result = result.plus("创建AI对局成功").plus("\n");
        result = result.plus("规则:横坐标为x，纵坐标为y，可以同时进行多盘棋。").plus("\n");
        result = result.plus("x从左往右为1-8，y从上往下为1-8。").plus("\n");
        result = result.plus("如显示有问题请用手机").plus("\n");
        result = result.plus("操作方式为输入指令[00+空格+x+空格+y]。").plus("\n");
        result = result.plus("空格为" + OthelloConstant.BLANK_NAME + "，黑方为" + OthelloConstant.BLACK_NAME + "，白方为" + OthelloConstant.WHITE_NAME).plus("\n");
        result = result.plus("从黑方开始，依次进行,超时时间为30秒，最后棋子多者胜利").plus("\n");
        result = result.plus("发送[黑白棋认输]可以结束已开始对局");
        ctx.group().sendMessage(result);
        code = othelloService.join(ctx.groupId(), AIId.addAndGet(1), "\uD83D\uDE02\uD83D\uDE02\uD83D\uDE02", ctx.group(), true);
        switch (code) {
            case OthelloService.YOU_IN_BOARD -> {
                ctx.group().sendMessage("你已经在对局中，请勿再参加了");
            }
            case OthelloService.YOU_CREATED_BOARD -> {
                ctx.group().sendMessage("不能自己和自己对局");
            }
            case OthelloService.OTHERS_CREATED_BOARD -> {
                ctx.group().sendMessage("已经有人创建了黑白棋了，快发送[加入黑白棋]开始对局吧");
            }
            case OthelloService.NOT_FOUND_BOARD -> ctx.group().sendMessage("请先创造黑白棋对局");
        }
    }
}
