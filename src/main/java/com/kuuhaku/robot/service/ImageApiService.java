package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.service.imageApi.ImageApiFactory;
import com.kuuhaku.robot.utils.HttpUtil;
import com.kuuhaku.robot.utils.HttpsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.UUID;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 17:52
 * @Description 使用api调用获取图片
 */
@Service
public class ImageApiService {

    /**
     * 最大尝试请求获取图片次数
     */
    public final int maxRequestTimes = 5;

    @Autowired
    private ImageApiFactory imageApiFactory;

    @Autowired
    private DownloadService downloadService;

    /**
     * 随机使用一个api获取图片
     * @return 本地图片路径
     */
    public String randomImagePath() {
        String path = null;
        for (int i = 0; i < maxRequestTimes; i++) {
            path = imageApiFactory.getRandomApi().getDownloadUri();
            if (path != null) {
                String localPath = imagePath(path);
                if (localPath != null) {
                    return localPath;
                }
            }
        }
        return null;
    }


    /**
     * 返回本地路径
     * @param urlStr 下载的uri
     * @return 本地路径
     */
    private String imagePath(String urlStr) {
        //类型根据创建连接
        HttpURLConnection conn = null;
        try {
            if (urlStr.startsWith("https")) {
                conn = HttpsUtil.getHttpsURLConnection(urlStr, HttpUtil.REQUEST_METHOD_GET);
            } else {
                conn = HttpUtil.getHttpURLConnection(urlStr, HttpUtil.REQUEST_METHOD_GET, null);
            }
            InputStream inStream = conn.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            //把图片信息存下来，写入内存
            String uuid = UUID.randomUUID().toString();
            byte[] data = outStream.toByteArray();
            // 固定文件名
            String filePath = downloadService.getRandomPngPath();
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
