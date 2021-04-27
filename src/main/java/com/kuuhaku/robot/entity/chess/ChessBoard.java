package com.kuuhaku.robot.entity.chess;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/11 7:21
 * @Description 井字棋盘
 */
@Slf4j
public class ChessBoard {
    private final int length = 3;

    private final Chess[][] chessBoard = new Chess[length][length];
    private String black;
    private String red;
    private String flag;
    private int turn;
    private final static int MAX_TURNS = 60;
    private long createTime;
    private final static long MAX_ALIVE_TIME = 1000 * 60 * 30;

    private final Set<Chess> redSet = new HashSet<>(5);
    private final Set<Chess> blackSet = new HashSet<>(5);

    private ChessBoard() {}

    /**
     * new一个实例
     * @return 实例
     */
    public static ChessBoard instant() {
        return new ChessBoard();
    }

    /**
     * 初始化
     * @param red 红方
     * @param black 黑方
     */
    public void init(String red, String black) {
        this.red = red;
        this.black = black;
        this.flag = red;
        this.turn = 0;
        createTime = System.currentTimeMillis();
        chessBoard[0][0] = new Chess(ChessConstant.APPLE, 0,0, red);
        chessBoard[0][1] = new Chess(ChessConstant.MELON, 0,1, red);
        chessBoard[0][2] = new Chess(ChessConstant.STRAW, 0,2, red);
        redSet.add(chessBoard[0][0]);
        redSet.add(chessBoard[0][1]);
        redSet.add(chessBoard[0][2]);
        chessBoard[1][0] = new Chess(ChessConstant.EMPTY, 1,0, null);
        chessBoard[1][1] = new Chess(ChessConstant.EMPTY, 1,1, null);
        chessBoard[1][2] = new Chess(ChessConstant.EMPTY, 1,2, null);
        chessBoard[2][0] = new Chess(ChessConstant.SUN, 2,0, black);
        chessBoard[2][1] = new Chess(ChessConstant.MOON, 2,1, black);
        chessBoard[2][2] = new Chess(ChessConstant.STAR, 2,2, black);
        blackSet.add(chessBoard[2][0]);
        blackSet.add(chessBoard[2][1]);
        blackSet.add(chessBoard[2][2]);
    }

    /**
     * 从指令获取真实名字
     * @param chessName 指令名
     * @return 真实名字
     */
    private String getTrueChessName(String chessName) {
        switch (chessName) {
            case "苹果":
                return ChessConstant.APPLE;
            case "西瓜":
                return ChessConstant.MELON;
            case "草莓":
                return ChessConstant.STRAW;
            case "星星":
                return ChessConstant.STAR;
            case "太阳":
                return ChessConstant.SUN;
            case "月亮":
                return ChessConstant.MOON;
            default:
                return null;
        }
    }

    /**
     * 获取棋子
     * @param chessName 棋子名字
     * @param user 棋子所属用户
     * @return 棋子对象
     */
    private Chess getChess(String chessName, String user) {
        chessName = getTrueChessName(chessName);
        if (chessName == null) {
            return null;
        }
        if (chessName.equals(ChessConstant.EMPTY)) {
            log.info("获取空棋子");
            return null;
        }
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (chessBoard[i][j].getName().equals(chessName) && user.equals(chessBoard[i][j].getUser())) {
                    log.info("获取到了棋子{}", chessBoard[i][j].getName());
                    return chessBoard[i][j];
                }
            }
        }
        log.info("没获取到了棋子{}", chessName);
        return null;
    }

    private boolean up(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if (x < 1 || !chessBoard[x - 1][y].getName().equals(ChessConstant.EMPTY)) {
            return false;
        }
        exchange(chess, chessBoard[x - 1][y]);
        return true;
    }

    private boolean down(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if (x > 1 || !chessBoard[x + 1][y].getName().equals(ChessConstant.EMPTY)) {
            return false;
        }
        exchange(chess, chessBoard[x + 1][y]);
        return true;
    }

    private boolean left(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if (y < 1 || !chessBoard[x][y - 1].getName().equals(ChessConstant.EMPTY)) {
            return false;
        }
        exchange(chess, chessBoard[x][y - 1]);
        return true;
    }

    private boolean right(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if (y > 1 || !chessBoard[x][y + 1].getName().equals(ChessConstant.EMPTY)) {
            return false;
        }
        exchange(chess, chessBoard[x][y + 1]);
        return true;
    }

    private boolean leftUp(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if ((x == 1 && y == 1) || (x == 2 && y == 2)) {
            if (chessBoard[x - 1][y - 1].getName().equals(ChessConstant.EMPTY)) {
                exchange(chess, chessBoard[x - 1][y - 1]);
                return true;
            }
        }
        return false;
    }

    private boolean leftDown(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if ((x == 0 && y == 2) || (x == 1 && y == 1)) {
            if (chessBoard[x + 1][y - 1].getName().equals(ChessConstant.EMPTY)) {
                exchange(chess, chessBoard[x + 1][y - 1]);
                return true;
            }
        }
        return false;
    }

    private boolean rightUp(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if ((x == 2 && y == 0) || (x == 1 && y == 1)) {
            if (chessBoard[x - 1][y + 1].getName().equals(ChessConstant.EMPTY)) {
                exchange(chess, chessBoard[x - 1][y + 1]);
                return true;
            }
        }
        return false;
    }

    private boolean rightDown(Chess chess) {
        int x = chess.getX();
        int y = chess.getY();
        if ((x == 0 && y == 0) || (x == 1 && y == 1)) {
            if (chessBoard[x + 1][y + 1].getName().equals(ChessConstant.EMPTY)) {
                exchange(chess, chessBoard[x + 1][y + 1]);
                return true;
            }
        }
        return false;
    }

    /**
     * 交互两棋位置
     * @param one 棋一
     * @param two 棋二
     */
    private void exchange(Chess one, Chess two) {
        int x1 = one.getX(), y1 = one.getY();
        int x2 = two.getX(), y2 = two.getY();
        one.setX(x2);
        one.setY(y2);
        two.setX(x1);
        two.setY(y1);
        chessBoard[x1][y1] = two;
        chessBoard[x2][y2] = one;
    }

    /**
     * 检测对局是否完成
     * @return 若完成返回胜利者
     */
    public String checkComplete() {
        Set<Chess> setOne = new HashSet<>(5);
        setOne.add(chessBoard[0][0]);
        setOne.add(chessBoard[1][1]);
        setOne.add(chessBoard[2][2]);
        if (setOne.equals(redSet) || setOne.equals(blackSet)) {
            return chessBoard[0][0].getUser();
        }
        Set<Chess> setTwo = new HashSet<>(5);
        setTwo.add(chessBoard[0][2]);
        setTwo.add(chessBoard[1][1]);
        setTwo.add(chessBoard[2][0]);
        if (setTwo.equals(redSet) || setTwo.equals(blackSet)) {
            return chessBoard[0][2].getUser();
        }
        return null;
    }

    /**
     * 检测回合是否到达上限
     * @return 真即为到达
     */
    public boolean checkEnableDo() {
        if (turn > MAX_TURNS) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 下棋
     * @param user 用户
     * @param operateName 操作名
     * @param chessName 操作棋子
     * @return 是否操作成功
     */
    public boolean operate(String user, String operateName, String chessName) {
        if (!flag.equals(user)) {
            log.info("不是你的回合");
            return false;
        }
        Chess chess = getChess(chessName, user);
        if (chess == null) {
            log.info("此棋子不属于用户");
            return false;
        }
        switch (operateName) {
            case "上":
                return up(chess);
            case "下":
                return down(chess);
            case "左":
                return left(chess);
            case "右":
                return right(chess);
            case "左上":
                return leftUp(chess);
            case "左下":
                return leftDown(chess);
            case "右上":
                return rightUp(chess);
            case "右下":
                return rightDown(chess);
            default:
                log.info(operateName + "操作不支持");
                return false;
        }
    }

    /**
     * 改变当前回合
     */
    public void changeTurn() {
        if (flag.equals(red)) {
            flag = black;
        } else {
            flag = red;
        }
        turn++;
    }

    /**
     * 检测是否到达上限时间
     * @return 真即为到达
     */
    public boolean checkAlive() {
        if (System.currentTimeMillis() - createTime > MAX_ALIVE_TIME) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 棋盘信息转为message
     * @param messageChain 原message
     * @return 拼接后的message
     */
    public MessageChain toMessage(MessageChain messageChain) {
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                messageChain = messageChain.plus(chessBoard[i][j].getName());
            }
            messageChain = messageChain.plus("\n");
        }
        return messageChain;
    }
}
