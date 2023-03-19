package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author by kuuhaku
 * @date 2022/5/31 20:24
 * @description
 */
@Getter
@Setter
@ToString
public class DynamicLive {
    private Long roomid;
    private Long uid;
    // 物述有栖-爱丽丝搬运
    // 4200下有
    private String uname;
    private String verify;
    private Long virtual;
    // 图片，必然有
    private String cover;
    // 4200下有
    private String live_time;
    // 4308下有
    private Long live_start_time;
    private Long round_status;
    private Long on_flag;
    // 【转文美\\/杂谈咖啡店】聚在一起聊什么呢？ 必然有
    private String title;
    private String tags;
    private String lock_status;
    private String hidden_status;
    private String user_cover;
    private Long short_id;
    private Long online;
    private Long area;
    private Long area_v2_id;
    private Long area_v2_parent_id;
    private Long attentions;
    private String background;
    private Long room_silent;
    private Long room_shield;
    private String try_time;
    private String area_v2_name;
    private String first_live_time;
    private Long live_id;
    private Long live_status;
    private String area_v2_parent_name;
    private Long broadcast_type;
    private String face;
    // 长链 必然有
    private String link;
    // 短链 4020下有
    private String slide_link;
    // "3342人看过" 必然有
    private String watched_show;
}
