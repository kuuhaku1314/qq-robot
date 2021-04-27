package com.kuuhaku.robot.service.imageApi;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 18:16
 * @Description 韩小韩API
 */
@Service
@Slf4j
@Primary
public class ApiTwo implements CommonImageApi {
    @Override
    public String getDownloadUri() {
        try {
            String json = HttpUtil.get("https://api.vvhan.com/api/acgimg?type=json");
            JSONObject jsonObject = JSONObject.parseObject(json);
            Boolean status = jsonObject.getBoolean("success");
            String img = jsonObject.getString("imgurl");
            if (status != null && status) {
                if (img != null) {
                    log.info("从韩小韩获取图片URI=[{}]", img);
                }
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        ApiTwo apiTwo = new ApiTwo();
        //apiTwo.getDownloadUri();
        System.out.println(System.getProperty("user.dir"));
        InputStream imgOneStream = new FileInputStream("src\\main\\resources\\image\\petpet\\frame0.png");
        int available = imgOneStream.available();
        System.out.println(available);
        byte[] bytes = new byte[200];
        imgOneStream.read(bytes);
    }
}
