package com.kuuhaku.robot.biliClient;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.biliClient.client.BaseClient;
import com.kuuhaku.robot.biliClient.exception.BiliRequestException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

public class BiliCall {
    public static BiliResult doCall(HttpRequestBase httpRequest, BaseClient baseClient) {
        try {
            HttpClient httpClient = baseClient.getHttpClient();
            RequestConfig requestConfig = baseClient.getRequestConfig();
            httpRequest.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(httpRequest);
            String body = EntityUtils.toString(response.getEntity());
            if (body == null || body.isEmpty())
                throw new Exception("响应体为空");
            return JSONObject.parseObject(body, BiliResult.class).check();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BiliRequestException(httpRequest.getURI(), e.getMessage());
        }
    }

    public static BiliResult doCall(BiliRequest request) {
        return doCall(new HttpGet(request.getURI()), request.getBaseClient());
    }
}
