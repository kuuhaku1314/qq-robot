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
 * @Date 2021/2/13 18:17
 * @Description 呓喵酱API
 */
@Service
@Slf4j
@Primary
public class ApiSix implements CommonImageApi {
    @Override
    public String getDownloadUri() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.yimian.xyz/img?type=moe&R18=false")
                    .openConnection();
            //设置为不对http链接进行重定向处理
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            //得到请求头的所有属性和值
            Map<String, List<String>> map = conn.getHeaderFields();
            Set<String> stringSet = map.keySet();
            //返回重定向的链接（父类UrlConnection的方法）
            String img = conn.getHeaderField("Location");
            log.info("从呓喵酱API获取图片URI=[{}]", img);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        ApiSix apiSix = new ApiSix();
        apiSix.getDownloadUri();
    }
}
