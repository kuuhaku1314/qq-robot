package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author by kuuhaku
 * @date 2022/5/26 23:14
 * @description
 */
@Getter
@Setter
@ToString
// 发布视频动态，type=8
public class DynamicVideo {
    private long aid;
    private int attribute;
    private long cid;
    private String comment_jump_url;
    private int copyright;
    private long ctime;
    // 视频内描述内容
    private String desc;
    private Object dimension;
    private int duration;
    // 动态内容描述
    private String dynamic;
    // 第一帧图片地址
    private String first_frame;
    private String jump_url;
    private Object owner;
    // 应该是分享视频截取的图片地址
    private String pic;
    private String player_info;
    private long pubdate;
    private Object rights;
    // 跳转短链1
    private String short_link;
    // 跳转短链2
    private String short_link_v2;
    private Object stat;
    private int state;
    private int tid;
    // 视频标题
    private String title;
    // tag名称
    private String tname;
    private int up_from_v2;
    private int videos;
}
