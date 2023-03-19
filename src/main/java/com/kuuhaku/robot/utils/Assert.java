package com.kuuhaku.robot.utils;

public class Assert {
    public static void IsTrue(boolean flag) {
        if (!flag) {
            throw new RuntimeException("is false");
        }
    }
}
