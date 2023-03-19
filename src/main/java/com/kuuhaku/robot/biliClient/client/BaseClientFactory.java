package com.kuuhaku.robot.biliClient.client;

public class BaseClientFactory {
    public static BaseClient getBaseClient() {
        return new BaseClientDefault();
    }
}
