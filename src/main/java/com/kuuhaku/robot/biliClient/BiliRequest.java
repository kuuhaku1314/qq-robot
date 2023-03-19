package com.kuuhaku.robot.biliClient;

import com.kuuhaku.robot.biliClient.client.BaseClient;
import com.kuuhaku.robot.biliClient.client.BaseClientFactory;
import com.kuuhaku.robot.biliClient.exception.BiliRequestException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class BiliRequest {
    private static final Logger log = LoggerFactory.getLogger(com.kuuhaku.robot.biliClient.BiliRequest.class);

    private final BaseClient baseClient;

    private final URIBuilder uriBuilder;

    public BiliRequest() {
        this.baseClient = BaseClientFactory.getBaseClient();
        this
                .uriBuilder = (new URIBuilder()).setScheme(this.baseClient.getProtocol()).setHost(this.baseClient.getHost()).setPort(this.baseClient.getPort());
    }

    public BiliRequest(BaseClient client, String version) {
        this.baseClient = client;
        this
                .uriBuilder = (new URIBuilder()).setScheme(client.getProtocol()).setHost(client.getHost()).setPort(client.getPort());
    }

    public com.kuuhaku.robot.biliClient.BiliRequest useHostVC() {
        this.uriBuilder.setHost(this.baseClient.getHostVC());
        return this;
    }

    public com.kuuhaku.robot.biliClient.BiliRequest setHost(String host) {
        this.uriBuilder.setHost(host);
        return this;
    }

    public com.kuuhaku.robot.biliClient.BiliRequest setPath(String path) {
        this.uriBuilder.setPath(path);
        return this;
    }

    public com.kuuhaku.robot.biliClient.BiliRequest setParams(String key, String value) {
        this.uriBuilder.setParameter(key, value);
        return this;
    }

    public com.kuuhaku.robot.biliClient.BiliRequest setParams(String key, Object value) {
        this.uriBuilder.setParameter(key, (value == null) ? "" : value.toString());
        return this;
    }

    public com.kuuhaku.robot.biliClient.BiliRequest setParams(List<NameValuePair> params) {
        this.uriBuilder.setParameters(params);
        return this;
    }

    public URI getURI() {
        try {
            return this.uriBuilder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("构建URI错误");
            throw new BiliRequestException(this.uriBuilder.getPath(), "URI构建错误");
        }
    }

    public BaseClient getBaseClient() {
        return this.baseClient;
    }

    public String getRequestPath() {
        return this.uriBuilder.getScheme() + "://" + this.uriBuilder.getHost() + ":" + this.uriBuilder.getPort() + this.uriBuilder
                .getPath();
    }
}
