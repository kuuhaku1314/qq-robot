package com.kuuhaku.robot.biliClient.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Nameplate {
    private Long nid;

    private String name;

    private String image;

    private String image_small;

    private String level;

    private String condition;
}
