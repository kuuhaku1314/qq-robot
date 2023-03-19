package com.kuuhaku.robot.utils;

/**
 * @author by kuuhaku
 * @date 2022/3/10 17:58
 * @description
 */
public class ThreadUtil {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
