package com.kuuhaku.robot.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

/**
 * @author by kuuhaku
 * @Date 2021/4/24 20:26
 * @Description 判断是否通过概率判断
 */
public class RandomUtil {

    /**
     * 通过概率
     * @param probability 概率百分比
     * @return 是否通过
     */
    public static boolean isPass(int probability) {
        int nextInt = new Random(System.currentTimeMillis()).nextInt(100) + 1;
        return nextInt <= probability;
    }

    /**
     * 获取随机数0-max-1
     * @param max 右边边界
     * @return 随机数
     */
    public static int random(int max) {
        return new Random(System.currentTimeMillis()).nextInt(max);
    }

    /**
     * 获取string参数的数字值，若不是指定范围内的值，则返回最小值
     * @param times 需要校验的值
     * @param min 最小值
     * @param max 最大值
     * @return int值
     */
    public static int getTimes(String times, int min, int max) {
        int i = min;
        if (StringUtils.isNumeric(times)) {
            int anInt = Integer.parseInt(times);
            if (min <= anInt && anInt <= max) {
                i = anInt;
            }
        }
        return i;
    }
}
