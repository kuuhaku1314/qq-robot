package com.kuuhaku.robot.biliClient.api.video;

import com.kuuhaku.robot.biliClient.BiliCall;
import com.kuuhaku.robot.biliClient.BiliRequest;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.model.video.Video;

public class VideoGet implements Gettable<Video> {
    private final BiliRequest request;

    public VideoGet(BiliRequest request) {
        this.request = request;
    }

    public Video get() {
        return BiliCall.doCall(this.request).toData(Video.class);
    }
}
