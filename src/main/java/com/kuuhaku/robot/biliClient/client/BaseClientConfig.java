package com.kuuhaku.robot.biliClient.client;

import org.apache.http.client.config.RequestConfig;

public class BaseClientConfig {
    public com.kuuhaku.robot.biliClient.client.BaseClientConfig setProtocol(String protocol) {
        if (protocol != null)
            BaseClientDefault.protocol = protocol;
        return this;
    }

    public com.kuuhaku.robot.biliClient.client.BaseClientConfig setHost(String host) {
        if (host != null)
            BaseClientDefault.host = host;
        return this;
    }

    public com.kuuhaku.robot.biliClient.client.BaseClientConfig setHostVc(String hostVc) {
        if (hostVc != null)
            BaseClientDefault.hostVc = hostVc;
        return this;
    }

    public com.kuuhaku.robot.biliClient.client.BaseClientConfig setPort(Integer port) {
        if (port != null)
            BaseClientDefault.port = port;
        return this;
    }

    public com.kuuhaku.robot.biliClient.client.BaseClientConfig setRequestConfig(RequestConfig config) {
        if (config != null)
            BaseClientDefault.config = config;
        return this;
    }
}
