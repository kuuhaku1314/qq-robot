package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Dynamic {
    private DType type;

    private DynamicBase base;

    private DynamicImage image;

    private DynamicRepost repost;

    private DynamicVideo video;

    private DynamicWord word;

    private DynamicDisplay display;

    private DynamicLive live;

    private DynamicActivity activity;

    private DynamicNotSupported notSupported;


    public enum DType {
        WORD, REPOST, VIDEO, IMAGE, NOT_SUPPORTED, LIVE, ACTIVITY
    }
}
