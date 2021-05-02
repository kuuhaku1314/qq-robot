package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.chain.Command;
import com.kuuhaku.robot.entity.chess.ChessBoard;
import com.kuuhaku.robot.entity.chess.ChessConstant;
import com.kuuhaku.robot.entity.chess.ChessGroup;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.service.ChessService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 2:03
 * @Description 井字棋
 */
@HandlerComponent
@Slf4j
public class ChessHandler {
    public static Map<String, ChessGroup> chessGroupMap = new ConcurrentHashMap<>();
    @Autowired
    private ChessService chessService;

    @Permission
    @Handler(values = {"创建井字棋"}, types = {HandlerMatchType.COMPLETE})
    public void createBattle(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        ChessGroup chessGroup = chessGroupMap.get(ctx.groupIdStr());
        if (chessGroup != null && chessGroup.getRed() != null) {
            result = result.plus("当前正有人在创建对局");
        } else if (!chessService.checkUser(ctx.senderIdStr())) {
            result = result.plus("你已经在对局中了，请勿再次创建");
        } else if (!checkUser(ctx.senderIdStr())) {
            result = result.plus("你在别的群创建了，请勿再次创建");
        } else {
            if (chessGroup != null) {
                chessGroup.setRed(ctx.senderIdStr());
                chessGroup.setRedNick(ctx.nickname());
            } else {
                chessGroup = new ChessGroup();
                chessGroup.setGroupId(ctx.groupIdStr());
                chessGroup.setRed(ctx.senderIdStr());
                chessGroup.setRedNick(ctx.nickname());
                chessGroupMap.put(ctx.groupIdStr(), chessGroup);
            }
            log.info("当前对局所在群为{}", chessGroup.getGroupId());
            result = result.plus("创建对局成功，需要参加此对局请发送[加入井字棋]。").plus("\n");
            result = result.plus("规则:红棋有 草莓 西瓜 苹果，黑棋有 太阳 月亮 星星。").plus("\n");
            result = result.plus("操作方式有 上下左右，左上左下右上右下。").plus("\n");
            result = result.plus("操作指令: 棋名+空格+操作方式，如 [草莓 左上]。").plus("\n");
            result = result.plus("红方开始，依次进行，60回合或30分钟则流局，达成三个棋子摆成一条斜线则胜利。").plus("\n");
            result = result.plus("发送[井字棋认输]可以结束已开始对局").plus("\n");
            result = result.plus("发送[停止井字棋]可以停止创建").plus("\n");
        }
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"加入井字棋"}, types = {HandlerMatchType.COMPLETE})
    public void joinBattle(ChannelContext ctx) {
        String user = ctx.senderIdStr();
        String group = ctx.groupIdStr();
        MessageChain result = MessageUtils.newChain();
        ChessGroup chessGroup = chessGroupMap.get(group);
        if (chessGroup == null || chessGroup.getRed() == null) {
            result = result.plus("请先创造对局");
        } else if (chessGroup.getRed().equals(user)) {
            result = result.plus("不能自己和自己对局");
        } else if (!chessService.checkUser(user)) {
            result = result.plus("你已经在对局中了，请勿再次参加");
        } else if (!checkUser(user)) {
            result = result.plus("你在别的群创建了，请勿再次加入");
        } else {
            chessGroup.setBlack(user);
            chessGroup.setBlackNick(ctx.nickname());
            ChessBoard chessBoard = chessService.createChessBoard(chessGroup.getRed(), chessGroup.getBlack(), group);
            if (chessBoard == null) {
                result = result.plus("创建失败，请重新创建");
                chessGroup.setRed(null);
                chessGroup.setBlack(null);
            } else {
                result = result.plus("对局创建成功").plus("\n");
                At redAt = new At(Long.parseLong(chessGroup.getRed()));
                At blackAt = new At(Long.parseLong(chessGroup.getBlack()));
                result = result.plus("对局双方为").plus("\n").plus("红方:").plus(redAt).plus("\n");
                result = result.plus("黑方:").plus(blackAt);
                chessGroup.setRed(null);
                chessGroup.setBlack(null);
                ctx.group().getId();
                ctx.group().sendMessage(result);
                ctx.group().sendMessage(chessService.toMessage(user, group));
                return;
            }
        }
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"苹果","西瓜","草莓","星星","月亮","太阳"}, types = {HandlerMatchType.START,
            HandlerMatchType.START,HandlerMatchType.START,HandlerMatchType.START,
            HandlerMatchType.START,HandlerMatchType.START,})
    public void battle(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        Command command = ctx.command();
        if (command.paramSize() != 1) {
            return;
        }
        String operateResult = chessService.operate(ctx.senderIdStr(), ctx.groupIdStr(),
                command.baseCommand(), command.iterator().next());
        if (operateResult.equals(ChessConstant.SUCCESS)) {
            result = chessService.toMessage(ctx.senderIdStr(), ctx.groupIdStr());
        } else {
            result = result.plus(operateResult);
        }
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"井字棋认输"}, types = {HandlerMatchType.COMPLETE})
    public void give(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        String userTwo = chessService.cancelBattle(ctx.senderIdStr(), ctx.groupIdStr());
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
    @Handler(values = {"取消井字棋"}, types = {HandlerMatchType.COMPLETE})
    public void stop(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        ChessGroup chessGroup = chessGroupMap.get(ctx.groupIdStr());
        if (chessGroup != null && chessGroup.getRed() != null && chessGroup.getRed().equals(ctx.senderIdStr())) {
            chessGroup.setRed(null);
            chessGroup.setRedNick(null);
            result = result.plus("取消创建成功");
            ctx.group().sendMessage(result);
        }
    }


    private boolean checkUser(String user) {
        Collection<ChessGroup> values = chessGroupMap.values();
        ArrayList<ChessGroup> chessGroups = new ArrayList<>(values);
        for (ChessGroup chessGroup : chessGroups) {
            if (user.equals(chessGroup.getRed()) || user.equals(chessGroup.getBlack())) {
                return false;
            }
        }
        return true;
    }
}
