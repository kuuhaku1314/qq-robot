package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.chain.Command;
import com.kuuhaku.robot.entity.othello.OthelloBoard;
import com.kuuhaku.robot.entity.othello.OthelloConstant;
import com.kuuhaku.robot.entity.othello.OthelloGroup;
import com.kuuhaku.robot.service.OthelloService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 2:04
 * @Description 黑白棋
 */
@HandlerComponent
@Slf4j
public class OthelloHandler {
    public static Map<String, OthelloGroup> othelloGroupMap = new HashMap<>();
    @Autowired
    private OthelloService othelloService;

    @Permission
    @Handler(values = {"创建黑白棋"}, types = {HandlerMatchType.COMPLETE})
    public void createBattle(ChannelContext ctx) {
        String user = ctx.senderIdStr();
        String group = ctx.groupIdStr();
        MessageChain result = MessageUtils.newChain();
        OthelloGroup othelloGroup = othelloGroupMap.get(group);
        if (othelloGroup != null && othelloGroup.getBlack() != null) {
            result = result.plus("当前正有人在创建黑白棋对局，请参与对局");
        } else if (!othelloService.checkUser(user)) {
            result = result.plus("你已经在黑白棋对局中了，请勿再次创建");
        } else if (!checkUser(user)) {
            result = result.plus("你在别的群创建了黑白棋，无法再继续创建");
        } else {
            if (othelloGroup != null) {
                othelloGroup.setBlack(user);
                othelloGroup.setBlackNick(ctx.nickname());
            } else {
                othelloGroup = new OthelloGroup();
                othelloGroup.setGroupId(group);
                othelloGroup.setBlack(user);
                othelloGroup.setBlackNick(ctx.nickname());
                othelloGroupMap.put(group, othelloGroup);
            }
            log.info("当前对局所在群为{}", othelloGroup.getGroupId());
            result = result.plus("当前对局所在群为:" + othelloGroup.getGroupId()).plus("\n");
            result = result.plus("创建对局成功，需要参加此对局请发送[加入黑白棋]。").plus("\n");
            result = result.plus("规则:横坐标为x，纵坐标为y，可以同时进行多盘棋。").plus("\n");
            result = result.plus("x从左往右为1-8，y从上往下为1-8。").plus("\n");
            result = result.plus("如显示有问题请用手机，电脑可能有问题").plus("\n");
            result = result.plus("操作方式为输入指令[00+空格+x+空格+y]。").plus("\n");
            result = result.plus("空格为" + OthelloConstant.BLANK_NAME + "，黑方为" + OthelloConstant.BLACK_NAME + "，白方为" + OthelloConstant.WHITE_NAME).plus("\n");
            result = result.plus("从黑方开始，依次进行,30分钟则流局，最后哪方棋子多则胜利，相同则白方胜").plus("\n");
            result = result.plus("发送[黑白棋认输]可以结束已开始对局，发送[停止黑白棋]可以停止创建。");
        }
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"加入黑白棋"}, types = {HandlerMatchType.COMPLETE})
    public void joinBattle(ChannelContext ctx) {
        String user = ctx.senderIdStr();
        MessageChain result = MessageUtils.newChain();
        OthelloGroup othelloGroup = othelloGroupMap.get(ctx.groupIdStr());
        if (othelloGroup == null || othelloGroup.getBlack() == null) {
            result = result.plus("请先创造黑白棋对局");
        } else if (othelloGroup.getBlack().equals(user)) {
            result = result.plus("不能自己和自己对局");
        } else if (!othelloService.checkUser(user)) {
            result = result.plus("你已经在对局中，请勿再参加了");
        } else if (!checkUser(user)) {
            result = result.plus("你在其他群创建黑白棋了，请勿再次创建");
        } else {
            othelloGroup.setWhite(user);
            othelloGroup.setWhiteNick(ctx.nickname());
            OthelloBoard othelloBoard = othelloService.createChessBoard(
                    othelloGroup.getBlack(), othelloGroup.getWhite(), ctx.groupIdStr());
            if (othelloBoard == null) {
                result = result.plus("创建失败，请重新创建");
                othelloGroup.setWhite(null);
                othelloGroup.setBlack(null);
            } else {
                result = result.plus("对局创建成功").plus("\n");
                At whiteAt = new At(Long.parseLong(othelloGroup.getWhite()));
                At blackAt = new At(Long.parseLong(othelloGroup.getBlack()));
                result = result.plus("对局双方为黑方:").plus(blackAt).plus("\n");
                result = result.plus("白方:").plus(whiteAt).plus("\n");
                result = result.plus("黑方先下");
                othelloGroup.setWhite(null);
                othelloGroup.setBlack(null);
                ctx.group().getId();
                ctx.group().sendMessage(result);
                ctx.group().sendMessage(othelloService.toMessage(user, ctx.groupIdStr()));
                return;
            }
        }
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"00"}, types = {HandlerMatchType.START})
    public void battle(ChannelContext ctx) {
        Command command = ctx.command();
        String user = ctx.senderIdStr();
        String group = ctx.groupIdStr();
        MessageChain result = MessageUtils.newChain();
        if (command.paramSize() != 2 || !StringUtils.isNumeric(command.params().get(0)) ||
                !StringUtils.isNumeric(command.params().get(1))) {
            return;
        }
        // 坐标系里参数xy与code内相反
        String operateResult = othelloService.operate(user, group,
                Integer.parseInt(command.params().get(1)), Integer.parseInt(command.params().get(0)));
        if (operateResult.equals(OthelloConstant.SUCCESS)) {
            OthelloBoard chessBoard = othelloService.getChessBoard(user, group);
            result = othelloService.toMessage(user, group);
            // 注意可能为空了
            othelloService.changeStatus(chessBoard);
            boolean status = othelloService.checkStatus(chessBoard);
            if (status) {
                ctx.group().sendMessage(result);
            } else {
                othelloService.changeStatus(chessBoard);
                MessageChain singleMessages = MessageUtils.newChain();
                singleMessages = singleMessages.plus("当前对方无步数可下，故自动转为下一回合，请继续下");
                ctx.group().sendMessage(singleMessages);
                ctx.group().sendMessage(othelloService.toMessage(user, group));
                if (!othelloService.checkStatus(chessBoard)) {
                    MessageChain end = othelloService.end(user, group);
                    ctx.group().sendMessage(end);
                }
            }
        } else {
            result = result.plus(operateResult);
            ctx.group().sendMessage(result);
        }
    }

    @Permission
    @Handler(values = {"黑白棋认输"}, types = {HandlerMatchType.COMPLETE})
    public void give(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        String userTwo = othelloService.cancelBattle(ctx.senderIdStr(), ctx.groupIdStr());
        if (userTwo == null) {
            return;
        }
        At at = new At(ctx.senderId());
        At atTwo = new At(Long.parseLong(userTwo));
        result = result.plus("").plus(at).plus("认输成功").plus("\n");
        result = result.plus("本次获胜者是").plus(atTwo);
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"取消黑白棋"}, types = {HandlerMatchType.COMPLETE})
    public void stop(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        OthelloGroup othelloGroup = othelloGroupMap.get(ctx.groupIdStr());
        if (othelloGroup != null && othelloGroup.getBlack() != null && othelloGroup.getBlack().equals(ctx.senderIdStr())) {
            othelloGroup.setBlack(null);
            othelloGroup.setBlackNick(null);
            result = result.plus("取消创建成功");
            ctx.group().sendMessage(result);
        }
    }

    private boolean checkUser(String user) {
        Collection<OthelloGroup> values = othelloGroupMap.values();
        ArrayList<OthelloGroup> othelloGroups = new ArrayList<>(values);
        for (OthelloGroup othelloGroup : othelloGroups) {
            if (user.equals(othelloGroup.getWhite()) || user.equals(othelloGroup.getBlack())) {
                return false;
            }
        }
        return true;
    }
}
