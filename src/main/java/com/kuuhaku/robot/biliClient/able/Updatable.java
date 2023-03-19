package com.kuuhaku.robot.biliClient.able;

public interface Updatable<T> extends BiliAbel<T> {
    T update(T paramT);
}
