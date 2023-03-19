package com.kuuhaku.robot.biliClient.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class LiveRoom {
    private Integer roomStatus;

    private Integer liveStatus;

    private String url;

    private String title;

    private String cover;

    private Long online;

    private Long roomid;

    private Integer roundStatus;

    private Integer broadcast_type;
}
