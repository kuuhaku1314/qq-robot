package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author by kuuhaku
 * @date 2022/5/27 2:22
 * @description
 */
@ToString
@Getter
@Setter
public class Picture {
    private String img_src;

    private Double img_size;

    private Integer img_width;

    private Integer img_height;

    private Object img_tags;
}
