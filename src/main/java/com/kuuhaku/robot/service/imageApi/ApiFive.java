package com.kuuhaku.robot.service.imageApi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 18:16
 * @Description 三秋API
 */

@Slf4j
@Service
@Primary
public class ApiFive implements CommonImageApi {
    public static void main(String[] args) {
        ApiFive apiFive = new ApiFive();
        apiFive.getDownloadUri();
    }

    @Override
    public String getDownloadUri() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.ghser.com/random/api.php")
                    .openConnection();
            //设置为不对http链接进行重定向处理
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            //得到请求头的所有属性和值
            Map<String, List<String>> map = conn.getHeaderFields();
            Set<String> stringSet = map.keySet();
            //返回重定向的链接（父类UrlConnection的方法）
            String img = conn.getHeaderField("Location");
            log.info("从三秋API获取图片URI=[{}]", img);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
