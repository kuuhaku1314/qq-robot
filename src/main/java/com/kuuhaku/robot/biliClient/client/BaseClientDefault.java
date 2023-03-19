package com.kuuhaku.robot.biliClient.client;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

public class BaseClientDefault implements BaseClient {
    static String protocol = "https";

    static String host = "api.bilibili.com";

    static int port = 443;

    static RequestConfig config;

    static String hostVc = "api.vc.bilibili.com";

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getHostVC() {
        return hostVc;
    }

    public int getPort() {
        return port;
    }

    public RequestConfig getRequestConfig() {
        if (config != null)
            return config;
        return RequestConfig.custom()

                .setConnectTimeout(5000)

                .setConnectionRequestTimeout(5000)

                .setSocketTimeout(5000)

                .setRedirectsEnabled(true).build();
    }

    public HttpClient getHttpClient() {
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        return HttpClientBuilder.create().setDefaultCookieStore(basicCookieStore).build();
    }
}
