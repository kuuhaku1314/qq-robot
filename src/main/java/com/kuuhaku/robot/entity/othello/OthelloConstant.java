package com.kuuhaku.robot.entity.othello;

/**
 * @Author by kuuhaku
 * @Date 2021/2/11 14:07
 * @Description 黑白棋常量
 */
public class OthelloConstant {

    /**
     * 空格
     */
    public static final int BLANK = 0;

    /**
     * 黑棋
     */
    public static final int BLACK = 1;

    /**
     * 白棋
     */
    public static final int WHITE = 2;

    /**
     * 空格名字，即显示的图标
     */
    public static final String BLANK_NAME = "☁";

    /**
     * 黑棋名字，即显示的图标
     */
    public static final String BLACK_NAME = "\uD83D\uDCA3";

    /**
     * 白棋名字，即显示的图标
     */
    public static final String WHITE_NAME = "\uD83D\uDD25";

    /**
     * 状态
     */
    public static final int ENABLE = 1;

    /**
     * 不能下，可能是无法进行对局或下的位置不对
     */
    public static final int DISABLE = 2;

    /**
     * 操作类型下子
     */
    public static final int OPERATE_MESSAGE = 1;

    /**
     * 操作类型认输
     */
    public static final int ADMIT_DEFEAT_MESSAGE = 2;

    /**
     * 对局结束提醒消息
     */
    public static final String END_MESSAGE = "game-over";

    /**
     * 棋局状态
     */
    public static final int BOARD_STATUS_PREPARE = 1;
    public static final int BOARD_STATUS_START = 2;
    public static final int BOARD_STATUS_OVER = 3;
}
