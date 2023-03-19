package com.kuuhaku.robot.service.imageApi;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 18:03
 * @Description 岁月小筑api
 */
@Service
@Slf4j
@Primary
public class ApiOne implements CommonImageApi {


    public static void main(String[] args) {
        ApiOne apiOne = new ApiOne();
        apiOne.getDownloadUri();
    }

    @Override
    public String getDownloadUri() {
        try {
            String json = HttpUtil.get("https://img.xjh.me/random_img.php?return=json");
            JSONObject jsonObject = JSONObject.parseObject(json);
            Integer status = jsonObject.getInteger("result");
            String img = jsonObject.getString("img");
            if (status != null && status == HttpStatus.OK.value()) {
                if (img != null) {
                    log.info("从岁月小筑获取图片URI=[{}]", img);
                }
            }
            return "http:" + img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
