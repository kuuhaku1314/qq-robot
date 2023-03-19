package com.kuuhaku.robot.biliClient.model.video;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class VideoPage {
    private long cid;

    private long page;

    private String from;

    private String part;

    private String duration;

    private String vid;

    private String weblink;

    private Dimension dimension;

}
