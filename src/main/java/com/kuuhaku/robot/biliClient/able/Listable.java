package com.kuuhaku.robot.biliClient.able;

public interface Listable<T> extends BiliAbel<T> {
    T list();

    T list(Long paramLong1, Long paramLong2);

    T list(Long paramLong);

    default T listPage(Long size, Long page) {
        return list(size, Long.valueOf(((page.longValue() >= 1L) ? (page.longValue() - 1L) : 0L) * size.longValue()));
    }
}
