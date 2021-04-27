package com.kuuhaku.robot.common.constant;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 1:33
 * @Description 关键字匹配类型
 */
public class HandlerMatchType {

    /**
     * 完全匹配
     */
    public static final int COMPLETE = 0;

    /**
     * 开头匹配
     */
    public static final int START = 1;

    /**
     * 包含
     */
    public static final int CONTAINS = 2;

    /**
     * 结尾匹配
     */
    public static final int END = 3;

    /**
     * 所有message都匹配，优先度最低，此类为链式执行
     */
    public static final int ALL = 4;
}
