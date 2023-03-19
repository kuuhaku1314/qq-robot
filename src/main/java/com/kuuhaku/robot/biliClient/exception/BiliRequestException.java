package com.kuuhaku.robot.biliClient.exception;

import java.net.URI;

public class BiliRequestException extends RuntimeException {
    private URI uri;

    private String path;

    public BiliRequestException(URI uri) {
        this(uri, "未知错误");
    }

    public BiliRequestException(URI uri, String message) {
        super(message);
        this.uri = uri;
    }

    public BiliRequestException(String path, String message) {
        super(message);
        this.path = path;
    }

    public URI getUri() {
        return this.uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString() {
        return "BiliRequestException{uri=" + this.uri + ", path='" + this.path + '\'' + "} " + super

                .toString();
    }

    public void printStackTrace() {
        super.printStackTrace();
        if (this.path != null) {
            System.err.println("请求错误路径为---> " + this.path);
        } else {
            System.err.println("请求错误路径为---> " + this.uri.getScheme() + "://" + this.uri.getHost() + ":" + this.uri
                    .getPort() + this.uri.getPath());
        }
    }
}
