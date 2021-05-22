package com.kuuhaku.robot.entity.othello;

import lombok.Data;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/11 14:03
 * @Description 黑白棋盘
 */
@Data
public class OthelloBoard {
    public static final int LENGTH = 8;
    private String black;
    private String white;
    private int flag;
    private int[][] othelloBoard = new int[LENGTH][LENGTH];
    private long createTime;
    private final static long MAX_ALIVE_TIME = 1000 * 60 * 30;

    private OthelloBoard() {}

    /**
     * 创建新实例
     * @return 棋盘实例对象
     */
    public static OthelloBoard instant() {
        return new OthelloBoard();
    }

    /**
     * 初始化
     * @param black 黑方
     * @param white 白方
     */
    public void init(String black, String white) {
        this.black = black;
        this.white = white;
        this.flag = OthelloConstant.BLACK_STATUS;
        createTime = System.currentTimeMillis();
        othelloBoard[3][3] = OthelloConstant.WHITE;
        othelloBoard[4][4] = OthelloConstant.WHITE;
        othelloBoard[3][4] = OthelloConstant.BLACK;
        othelloBoard[4][3] = OthelloConstant.BLACK;
    }

    /**
     * 检测场中是否有能下棋子
     * @return 能或不能
     */
    public String checkStatus() {
        int chess = -1;
        if (flag == OthelloConstant.BLACK_STATUS) {
            chess = OthelloConstant.BLACK;
        }
        if (flag == OthelloConstant.WHITE_STATUS) {
            chess = OthelloConstant.WHITE;
        }
        int otherChess = chess == OthelloConstant.BLACK ? OthelloConstant.WHITE : OthelloConstant.BLACK;
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                if (checkEnable(chess, i, j, otherChess)) {
                    return OthelloConstant.SUCCESS;
                }
            }
        }
        return OthelloConstant.DISABLE;
    }

    /**
     * 操作
     * @param chess 操作棋子
     * @param x x下标
     * @param y y下标
     * @return 操作结果 成功或失败
     */
    public String operate(int chess, int x, int y) {
        if (x < 0 || x > LENGTH || y < 0 || y > LENGTH) {
            return OthelloConstant.DISABLE;
        }
        int otherChess = chess == OthelloConstant.BLACK ? OthelloConstant.WHITE : OthelloConstant.BLACK;
        int sum = 0;
        if (othelloBoard[x][y] == OthelloConstant.BLANK) {
            sum = sum + eatLeftAndRight(chess, x, y, otherChess, true);
            sum = sum + eatUpAndDown(chess, x, y, otherChess, true);
            sum = sum + eatCross(chess, x, y, otherChess, true);
            if (sum == 0) {
                return OthelloConstant.DISABLE;
            } else {
                return OthelloConstant.SUCCESS;
            }
        } else {
            return OthelloConstant.DISABLE;
        }
    }

    /**
     * 获取用户棋子类型
     * @param user 用户
     * @return 黑棋或白棋
     */
    public int getChess(String user) {
        if (white.equals(user)) {
            return OthelloConstant.WHITE;
        } else {
            return OthelloConstant.BLACK;
        }
    }

    /**
     * 是否是用户的回合
     * @param user 用户
     * @return 是否是
     */
    public boolean isTurn(String user) {
        if (user.equals(white)) {
            return flag == OthelloConstant.WHITE_STATUS;
        } else {
            return flag == OthelloConstant.BLACK_STATUS;
        }
    }

    /**
     * 改变回合状态，进入下一回合
     */
    public void changeStatus() {
        if (flag == OthelloConstant.BLACK_STATUS) {
            flag = OthelloConstant.WHITE_STATUS;
        } else {
            flag = OthelloConstant.BLACK_STATUS;
        }
    }

    /**
     * 检测当前位置能否落子
     * @param chess 棋子
     * @param x x下标
     * @param y y下标
     * @param otherChess 另一方的棋子类型
     * @return 真或假
     */
    public boolean checkEnable(int chess,int x, int y, int otherChess) {
        int sum = 0;
        if (othelloBoard[x][y] == OthelloConstant.BLANK) {
            sum = sum + eatLeftAndRight(chess, x, y, otherChess, false);
            sum = sum + eatUpAndDown(chess, x, y, otherChess, false);
            sum = sum + eatCross(chess, x, y, otherChess, false);
        }
        return sum != 0;
    }

    /**
     * 吃左右棋子
     * @param chess 棋子
     * @param x x
     * @param y y
     * @param otherChess 另一方棋子类型
     * @param trueDo 是真的吃掉还只是估计能吃多少
     * @return 能吃掉多少
     */
    private int eatLeftAndRight(int chess,int x, int y, int otherChess, boolean trueDo) {
        int eatChessNum = 0;
        int temp = 0;
        // 检查左边
        for (int i = y - 1; i >= 0 ; i--) {
            if (othelloBoard[x][i] == otherChess) {
                temp++;
            } else if (othelloBoard[x][i] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int j = i + 1; j < y; j++) {
                        othelloBoard[x][j] = chess;
                    }
                }
                break;
            } else if (othelloBoard[x][i] == OthelloConstant.BLANK) {
                break;
            }
        }
        temp = 0;
        // 检查右边
        for (int i = y + 1; i < LENGTH ; i++) {
            if (othelloBoard[x][i] == otherChess) {
                temp++;
            } else if (othelloBoard[x][i] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int j = i - 1; j > y; j--) {
                        othelloBoard[x][j] = chess;
                    }
                }
                break;
            } else if (othelloBoard[x][i] == OthelloConstant.BLANK) {
                break;
            }
        }
        return eatChessNum;
    }

    private int eatUpAndDown(int chess,int x, int y, int otherChess, boolean trueDo) {
        int eatChessNum = 0;
        int temp = 0;
        // 检查上边
        for (int i = x - 1; i >= 0 ; i--) {
            if (othelloBoard[i][y] == otherChess) {
                temp++;
            } else if (othelloBoard[i][y] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int j = i + 1; j < x; j++) {
                        othelloBoard[j][y] = chess;
                    }
                }
                break;
            } else if (othelloBoard[i][y] == OthelloConstant.BLANK) {
                break;
            }
        }
        temp = 0;
        // 检查下边
        for (int i = x + 1; i < LENGTH ; i++) {
            if (othelloBoard[i][y] == otherChess) {
                temp++;
            } else if (othelloBoard[i][y] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int j = i - 1; j > x; j--) {
                        othelloBoard[j][y] = chess;
                    }
                }
                break;
            } else if (othelloBoard[i][y] == OthelloConstant.BLANK) {
                break;
            }
        }
        return eatChessNum;
    }

    public int eatCross(int chess,int x, int y, int otherChess, boolean trueDo) {
        int eatChessNum = 0;
        int temp = 0;
        // 左上 x变小y变小
        for (int i = x - 1, j = y - 1; i >=0 && j >= 0; i--, j--) {
            if (othelloBoard[i][j] == otherChess) {
                temp++;
            } else if (othelloBoard[i][j] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int m = i + 1, n = j + 1; m < x; m++, n++) {
                        othelloBoard[m][n] = chess;
                    }
                }
                break;
            } else if (othelloBoard[i][j] == OthelloConstant.BLANK) {
                break;
            }
        }
        temp = 0;
        // 右上 x变小y变大
        for (int i = x - 1, j = y + 1; i >=0 && j < LENGTH; i--, j++) {
            if (othelloBoard[i][j] == otherChess) {
                temp++;
            } else if (othelloBoard[i][j] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int m = i + 1, n = j - 1; m < x; m++, n--) {
                        othelloBoard[m][n] = chess;
                    }
                }
                break;
            } else if (othelloBoard[i][j] == OthelloConstant.BLANK) {
                break;
            }
        }
        temp = 0;
        // 左下 x变大y变小
        for (int i = x + 1, j = y - 1; i < LENGTH && j >= 0; i++, j--) {
            if (othelloBoard[i][j] == otherChess) {
                temp++;
            } else if (othelloBoard[i][j] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int m = i - 1, n = j + 1; m > x; m--, n++) {
                        othelloBoard[m][n] = chess;
                    }
                }
                break;
            } else if (othelloBoard[i][j] == OthelloConstant.BLANK) {
                break;
            }
        }
        temp = 0;
        // 右下 x变大y变大
        for (int i = x + 1, j = y + 1; i < LENGTH && j < LENGTH; i++, j++) {
            if (othelloBoard[i][j] == otherChess) {
                temp++;
            } else if (othelloBoard[i][j] == chess) {
                eatChessNum = eatChessNum + temp;
                if (trueDo) {
                    for (int m = i - 1, n = j - 1; m > x; m--, n--) {
                        othelloBoard[m][n] = chess;
                    }
                }
                break;
            } else if (othelloBoard[i][j] == OthelloConstant.BLANK) {
                break;
            }
        }
        return eatChessNum;
    }

    /**
     * 检测棋局是否超时
     * @return 是否超时
     */
    public boolean checkAlive() {
        return System.currentTimeMillis() - createTime <= MAX_ALIVE_TIME;
    }


    /**
     * 拼接棋局信息
     * @param messageChain 信息
     * @return 拼接后的信息
     */
    public MessageChain toMessage(MessageChain messageChain) {
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                String line = "";
                if (othelloBoard[i][j] == OthelloConstant.BLANK) {
                    line = line + OthelloConstant.BLANK_NAME;
                } else if (othelloBoard[i][j] == OthelloConstant.BLACK) {
                    line = line + OthelloConstant.BLACK_NAME;
                } else if (othelloBoard[i][j] == OthelloConstant.WHITE) {
                    line = line + OthelloConstant.WHITE_NAME;
                }
                messageChain = messageChain.plus(line);
            }
            messageChain = messageChain.plus("\n");
        }
        return messageChain;
    }

    /**
     * 是否完成对局
     * @return 当前所剩空格子
     */
    public int checkComplete() {
        int blankNum = 0;
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                if (othelloBoard[i][j] == OthelloConstant.BLANK) {
                    blankNum++;
                }
            }
        }
        return blankNum;
    }

    /**
     * 获取黑白棋子数目及胜利者
     * @return black:white:user
     */
    public String getBlackAndWhiteAndVictoryUser() {
        int blackNum = 0;
        int whiteNum = 0;
        String victoryUser = "";
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < LENGTH; j++) {
                if (othelloBoard[i][j] == OthelloConstant.BLACK) {
                    blackNum++;
                } else if (othelloBoard[i][j] == OthelloConstant.WHITE) {
                    whiteNum++;
                }
            }
        }
        if (blackNum > whiteNum) {
            victoryUser = black;
        } else {
            victoryUser = white;
        }
        return "" + blackNum + ":" + whiteNum + ":" + victoryUser;
    }
}
