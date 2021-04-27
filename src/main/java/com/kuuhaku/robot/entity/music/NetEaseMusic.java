package com.kuuhaku.robot.entity.music;

import lombok.Data;

import java.util.List;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 21:28
 * @Description 网易云音乐实体
 */
@Data
public class NetEaseMusic {

    private String id;
    private String name;
    private List<String> artists;
    private String picUrl;

}
