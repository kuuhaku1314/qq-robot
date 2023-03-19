package com.kuuhaku.robot.biliClient.client;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;

public interface BaseClient {
    String getProtocol();

    String getHost();

    String getHostVC();

    int getPort();

    RequestConfig getRequestConfig();

    HttpClient getHttpClient();
}
