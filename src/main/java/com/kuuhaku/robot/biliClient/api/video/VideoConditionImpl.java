package com.kuuhaku.robot.biliClient.api.video;

import com.kuuhaku.robot.biliClient.BiliRequestFactor;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.model.video.Video;

public class VideoConditionImpl implements IVideoCondition {
    public Gettable<Video> withAvid(long av) {
        return new VideoGet(BiliRequestFactor.getBiliRequest().setPath("/x/web-interface/view").setParams("aid", av));
    }

    public Gettable<Video> withBvid(String bvid) {
        return new VideoGet(BiliRequestFactor.getBiliRequest().setPath("/x/web-interface/view").setParams("bvid", bvid));
    }
}
