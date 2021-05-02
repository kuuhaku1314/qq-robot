package com.kuuhaku.robot.service;

import com.kuuhaku.robot.entity.chess.ChessConstant;
import com.kuuhaku.robot.entity.othello.OthelloBoard;
import com.kuuhaku.robot.entity.othello.OthelloConstant;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/11 15:42
 * @Description 黑白棋service
 */
@Service
public class OthelloService {
    public final Map<String, OthelloBoard> othelloBoardGroup = new ConcurrentHashMap<>();

    public final Map<String, String> userBattleMap = new ConcurrentHashMap<>();

    public OthelloBoard getChessBoard(String user, String groupId) {
        String userTwo = userBattleMap.get(user);
        OthelloBoard othelloBoard = othelloBoardGroup.get(user + ":" + userTwo + ":" + groupId);
        if (othelloBoard == null) {
            othelloBoard = othelloBoardGroup.get(userTwo + ":" + user + ":" + groupId);
        }
        return othelloBoard;
    }

    public OthelloBoard createChessBoard(String black, String white, String groupId) {
        if (userBattleMap.containsKey(white) || userBattleMap.containsKey(black)) {
            return null;
        }
        OthelloBoard othelloBoard = OthelloBoard.instant();
        othelloBoard.init(black, white);
        othelloBoardGroup.put((black + ":" + white + ":" + groupId), othelloBoard);
        userBattleMap.put(white, black);
        userBattleMap.put(black, white);
        return othelloBoard;
    }

    public boolean checkUser(String user) {
        return !userBattleMap.containsKey(user);
    }

    public String operate(String user, String group, int x, int y) {
        String userTwo = userBattleMap.get(user);
        if (userTwo == null) {
            return "你没有参加对局";
        }
        OthelloBoard othelloBoard = othelloBoardGroup.get(user + ":" + userTwo + ":" + group);
        if (othelloBoard == null) {
            othelloBoard = othelloBoardGroup.get(userTwo + ":" + user + ":" + group);
        }
        if (othelloBoard == null) {
            userBattleMap.remove(user);
            userBattleMap.remove(userTwo);
            return "没有此对局";
        }
        if (!othelloBoard.checkAlive()) {
            userBattleMap.remove(user);
            userBattleMap.remove(userTwo);
            othelloBoardGroup.remove(user + ":" + userTwo + ":" + group);
            othelloBoardGroup.remove(userTwo + ":" + user + ":" + group);
            return "当前对局已结束";
        }
        boolean isTurn = othelloBoard.isTurn(user);
        if (!isTurn) {
            return "不是你的回合";
        }
        int chess = othelloBoard.getChess(user);
        // 代码里按下标从1开始
        String flag = othelloBoard.operate(chess, x - 1, y - 1);
        if (flag.equals(OthelloConstant.DISABLE)) {
            return "这个地方不能落子";
        } else {
            othelloBoard.getOthelloBoard()[x - 1][y - 1] = chess;
            return ChessConstant.SUCCESS;
        }
    }

    public MessageChain toMessage(String user, String group) {
        String userTwo = userBattleMap.get(user);
        OthelloBoard othelloBoard = othelloBoardGroup.get(user + ":" + userTwo + ":" + group);
        if (othelloBoard == null) {
            othelloBoard = othelloBoardGroup.get(userTwo + ":" + user + ":" + group);
        }
        MessageChain messageChain = MessageUtils.newChain();
        messageChain = messageChain.plus("当前对局情况为").plus("\n");
        messageChain = othelloBoard.toMessage(messageChain);
        int blankNum = othelloBoard.checkComplete();
        if (blankNum == 0) {
            String blackAndWhiteAndVictoryUser = othelloBoard.getBlackAndWhiteAndVictoryUser();
            String[] strings = blackAndWhiteAndVictoryUser.split(":");
            At at = new At(Long.parseLong(strings[2]));
            int blackNum = Integer.parseInt(strings[0]);
            int whiteNum = Integer.parseInt(strings[1]);
            messageChain = messageChain.plus("本次对局结束").plus("\n");
            messageChain = messageChain.plus("黑比白比分为:" + blackNum + ":" + whiteNum).plus("\n");
            messageChain = messageChain.plus("当前的获胜者是").plus(at).plus("\n");
            userBattleMap.remove(user);
            userBattleMap.remove(userTwo);
            othelloBoardGroup.remove(user + ":" + userTwo + ":" + group);
            othelloBoardGroup.remove(userTwo + ":" + user + ":" + group);
        }
        return messageChain;
    }

    public MessageChain end(String user, String group) {
        String userTwo = userBattleMap.get(user);
        OthelloBoard othelloBoard = othelloBoardGroup.get(user + ":" + userTwo + ":" + group);
        if (othelloBoard == null) {
            othelloBoard = othelloBoardGroup.get(userTwo + ":" + user + ":" + group);
        }
        MessageChain messageChain = MessageUtils.newChain();
        String blackAndWhiteAndVictoryUser = othelloBoard.getBlackAndWhiteAndVictoryUser();
        String[] strings = blackAndWhiteAndVictoryUser.split(":");
        At at = new At(Long.parseLong(strings[2]));
        int blackNum = Integer.parseInt(strings[0]);
        int whiteNum = Integer.parseInt(strings[1]);
        messageChain = messageChain.plus("当前双方都不可下，开始进行结算");
        messageChain = messageChain.plus("本次对局结束").plus("\n");
        messageChain = messageChain.plus("黑比白比分为:" + blackNum + ":" + whiteNum).plus("\n");
        messageChain = messageChain.plus("当前的获胜者是").plus(at).plus("\n");
        userBattleMap.remove(user);
        userBattleMap.remove(userTwo);
        othelloBoardGroup.remove(user + ":" + userTwo + ":" + group);
        othelloBoardGroup.remove(userTwo + ":" + user + ":" + group);
        return messageChain;
    }

    public String cancelBattle(String user, String group) {
        String userTwo = userBattleMap.get(user);
        if (userTwo == null) {
            return null;
        }
        userBattleMap.remove(user);
        userBattleMap.remove(userTwo);
        othelloBoardGroup.remove(user + ":" + userTwo + ":" + group);
        othelloBoardGroup.remove(userTwo + ":" + user + ":" + group);
        return userTwo;
    }

    public void changeStatus(OthelloBoard othelloBoard) {
        othelloBoard.changeStatus();
    }

    public boolean checkStatus(OthelloBoard othelloBoard) {
        String status = othelloBoard.checkStatus();
        return status.equals(OthelloConstant.SUCCESS);
    }

}
