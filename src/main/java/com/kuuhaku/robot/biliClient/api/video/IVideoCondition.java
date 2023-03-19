package com.kuuhaku.robot.biliClient.api.video;

import com.kuuhaku.robot.biliClient.BiliCondition;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.model.video.Video;

public interface IVideoCondition extends BiliCondition {
    Gettable<Video> withAvid(long paramLong);

    Gettable<Video> withBvid(String paramString);
}
