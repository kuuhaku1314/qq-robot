package com.kuuhaku.robot.biliClient.utils;

import java.util.Date;

public class TransDate {
    public static Date timestampToDate(Long timestamp) {
        if (timestamp != null && timestamp > 0L)
            return new Date(timestamp * 1000L);
        return new Date();
    }

    public static long nowTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    public static long dateToTimestamp(Date date) {
        if (date == null)
            return nowTimestamp();
        return date.getTime() / 1000L;
    }
}
