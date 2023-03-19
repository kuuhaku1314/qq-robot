package com.kuuhaku.robot.biliClient.model.video;

import com.kuuhaku.robot.biliClient.model.BaseModel;
import com.kuuhaku.robot.biliClient.model.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
public class Video implements BaseModel {
    private String bvid;

    private long aid;

    private long videos;

    private long tid;

    private String tname;

    private Integer copyright;

    private String pic;

    private String title;

    private long pubdate;

    private long ctime;

    private String desc;

    private Integer state;

    private Boolean no_cache;

    private Long duration;

    private Object rights;

    private User owner;

    private VideoData stat;

    private String dynamic;

    private long cid;

    private Dimension dimension;

    private List<VideoPage> pages;

    private Object subtitle;

    private List<User> staff;

    private Object user_garb;
}
