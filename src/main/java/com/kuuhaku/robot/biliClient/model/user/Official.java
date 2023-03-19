package com.kuuhaku.robot.biliClient.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Official {
    private Integer role;

    private String title;

    private String desc;

    private Integer type;
}
