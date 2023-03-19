package com.kuuhaku.robot.biliClient.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Pendant {
    private Long pid;

    private String name;

    private String image;

    private Long expire;

    private String image_enhance;

    private String image_enhance_frame;
}
