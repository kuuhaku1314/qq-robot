package com.kuuhaku.robot.biliClient.utils;

import com.kuuhaku.robot.biliClient.BiliClient;
import com.kuuhaku.robot.biliClient.BiliClientFactor;
import com.kuuhaku.robot.biliClient.model.dynamic.Dynamic;
import com.kuuhaku.robot.biliClient.model.user.User;
import com.kuuhaku.robot.biliClient.model.video.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransViewUri {
    private static final Logger log = LoggerFactory.getLogger(com.kuuhaku.robot.biliClient.utils.TransViewUri.class);

    public static String trans(User user) {
        return "https://space.bilibili.com/" + user.getMid();
    }

    public static String trans(Dynamic dynamic) {
        if (dynamic != null && dynamic.getBase() != null)
            return "https://t.bilibili.com/" + dynamic.getBase().getDynamic_id();
        return null;
    }

    public static String trans(Video video) {
        if (video != null) {
            if (!video.getBvid().isEmpty())
                return "https://www.bilibili.com/video/" + video.getBvid();
            if (video.getAid() != 0L)
                return "https://www.bilibili.com/video/av" + video.getAid();
        }
        return null;
    }

    public static String AvidToBvid(long avid) {
        Video video = null;
        try {
            BiliClient biliClient = BiliClientFactor.getClient();
            video = biliClient.video().withAvid(avid).get();
            if (video != null)
                return video.getBvid();
        } catch (Exception e) {
            log.error("转换错误，原始id为 {},对于视频信息为{},错误原因为{}", avid, video, e);
            e.printStackTrace();
        }
        return null;
    }

    public static long BvidToAvid(String bvid) {
        Video video = null;
        try {
            BiliClient biliClient = BiliClientFactor.getClient();
            video = biliClient.video().withBvid(bvid).get();
            if (video != null)
                return video.getAid();
        } catch (Exception e) {
            log.error("转换错误，原始id为 {},对于视频信息为{},错误原因为{}", bvid, video, e);
            e.printStackTrace();
        }
        return 0L;
    }
}
