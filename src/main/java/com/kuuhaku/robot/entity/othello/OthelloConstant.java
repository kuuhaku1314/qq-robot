package com.kuuhaku.robot.entity.othello;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/11 14:07
 * @Description 黑白棋常量
 */
public class OthelloConstant {

    /**
     * 轮到黑方下子
     */
    public static final int BLACK_STATUS = 0;

    /**
     * 轮到白方下子
     */
    public static final int WHITE_STATUS = 1;

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
    public static final String SUCCESS = "success";

    /**
     * 不能下，可能是无法进行对局或下的位置不对
     */
    public static final String DISABLE = "disable";

    public static final String NOT_TURN = "notTurn";

}
