package com.kuuhaku.robot.biliClient.model.video;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class VideoData {
    private long aid;

    private long danmaku;

    private long view;

    private long reply;

    private long coin;

    private long like;

    private long favorite;

    private long share;

    private long now_rank;

    private long his_rank;

    private long dislike;

    private String evaluation;

}
