package com.kuuhaku.robot.service;

import com.kuuhaku.robot.entity.chess.ChessBoard;
import com.kuuhaku.robot.entity.chess.ChessConstant;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/11 8:46
 * @Description 下井字棋
 */
@Service
public class ChessService {

    /**
     * key red:black:groupId
     */
    public final Map<String, ChessBoard> chessBoardGroup = new HashMap<>();

    /**
     * 对战者与其对手的关系map
     */
    public final Map<String, String> userBattleMap = new HashMap<>();

    public synchronized ChessBoard createChessBoard(String red, String black, String group) {
        if (userBattleMap.containsKey(red) || userBattleMap.containsKey(black)) {
            return null;
        }
        ChessBoard chessBoard = ChessBoard.instant();
        chessBoard.init(red, black);
        chessBoardGroup.put((red + ":" + black + ":" + group), chessBoard);
        userBattleMap.put(red, black);
        userBattleMap.put(black, red);
        return chessBoard;
    }

    public boolean checkUser(String user) {
        return !userBattleMap.containsKey(user);
    }

    public String operate(String user, String group, String operateName, String chessName) {
        String userTwo = userBattleMap.get(user);
        if (userTwo == null) {
            return "你没有参加对局";
        }
        ChessBoard chessBoard = chessBoardGroup.get(user + ":" + userTwo + ":" + group);
        if (chessBoard == null) {
            chessBoard = chessBoardGroup.get(userTwo + ":" + user + ":" + group);
        }
        if (chessBoard == null) {
            userBattleMap.remove(user);
            userBattleMap.remove(userTwo);
            return "没有此对局";
        }
        if (!chessBoard.checkAlive() || !chessBoard.checkEnableDo()) {
            userBattleMap.remove(user);
            userBattleMap.remove(userTwo);
            chessBoardGroup.remove(user + ":" + userTwo+ ":" + group);
            chessBoardGroup.remove(userTwo + ":" + user+ ":" + group);
            return "当前对局已结束";
        }
        boolean flag = chessBoard.operate(user, operateName, chessName);
        if (flag) {
            chessBoard.changeTurn();
            return ChessConstant.SUCCESS;
        } else {
            return "指令格式异常";
        }
    }

    public MessageChain toMessage(String user, String group) {
        String userTwo = userBattleMap.get(user);
        ChessBoard chessBoard = chessBoardGroup.get(user + ":" + userTwo + ":" + group);
        if (chessBoard == null) {
            chessBoard = chessBoardGroup.get(userTwo + ":" + user + ":" + group);
        }
        MessageChain messageChain = MessageUtils.newChain();
        messageChain = messageChain.plus("当前对局情况为").plus("\n");
        messageChain = chessBoard.toMessage(messageChain);
        String victoryUser = chessBoard.checkComplete();
        if (victoryUser != null) {
            At at = new At(Long.parseLong(victoryUser));
            messageChain = messageChain.plus("当前的获胜者是").plus(at).plus("\n");
            messageChain = messageChain.plus("本次对局结束");
            userBattleMap.remove(user);
            userBattleMap.remove(userTwo);
            chessBoardGroup.remove(user + ":" + userTwo + ":" + group);
            chessBoardGroup.remove(userTwo + ":" + user + ":" + group);
        }
        return messageChain;
    }

    public String cancelBattle(String user, String group) {
        String userTwo = userBattleMap.get(user);
        if (userTwo == null) {
            return null;
        }
        userBattleMap.remove(user);
        userBattleMap.remove(userTwo);
        chessBoardGroup.remove(user + ":" + userTwo + ":" + group);
        chessBoardGroup.remove(userTwo + ":" + user + ":" + group);
        return userTwo;
    }

}
