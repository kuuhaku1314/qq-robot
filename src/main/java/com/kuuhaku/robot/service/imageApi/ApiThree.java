package com.kuuhaku.robot.service.imageApi;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.utils.HttpsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 18:16
 * @Description 樱花
 */
@Service
@Slf4j
@Primary
public class ApiThree implements CommonImageApi {
    @Override
    public String getDownloadUri() {
        try {
            String json = new String(HttpsUtil.doGet("https://www.dmoe.cc/random.php?return=json"));
            JSONObject jsonObject = JSONObject.parseObject(json);
            Integer status = jsonObject.getInteger("code");
            String img = jsonObject.getString("imgurl");
            if (status != null && status == HttpStatus.OK.value()) {
                if (img != null) {
                    log.info("从樱花获取图片URI=[{}]", img);
                }
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        ApiThree apiThree = new ApiThree();
        apiThree.getDownloadUri();
    }
}
