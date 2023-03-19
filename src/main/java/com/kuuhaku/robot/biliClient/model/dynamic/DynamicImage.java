package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@ToString
@Getter
@Setter
// type为2下图片动态
public class DynamicImage {
    private Long id;
    // 看起来是空
    private String title;

    // 内容
    private String description;

    private String category;

    private Object role;

    private Object source;

    private List<Picture> pictures;

    private Integer pictures_count;

    // 上传时间
    private Long upload_time;

    // 回复数量
    private Long reply;

    private Object settings;

    private Integer is_fav;

}
