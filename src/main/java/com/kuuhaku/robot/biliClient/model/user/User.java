package com.kuuhaku.robot.biliClient.model.user;

import com.kuuhaku.robot.biliClient.model.LiveRoom;
import com.kuuhaku.robot.biliClient.model.SysNotice;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class User {
    private Long mid;

    private String name;

    private String sex;

    private String face;

    private String sign;

    private Long rank = 1000L;

    private Integer level;

    private Integer silence;

    private String birthday;

    private Long coins;

    private Boolean fans_badge;

    private Official official;

    private Vip vip;

    private Pendant pendant;

    private Nameplate nameplate;

    private Boolean is_followed;

    private String top_photo;

    private Object theme;

    private SysNotice sys_notice;

    private LiveRoom live_room;
}
