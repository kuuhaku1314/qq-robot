package com.kuuhaku.robot.biliClient.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class SysNotice {
    private Integer id;

    private String content;

    private String url;

    private Integer notice_type;

    private String icon;

    private String text_color;

    private String bg_color;

}
