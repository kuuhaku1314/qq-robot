package com.kuuhaku.robot.biliClient.model;

import java.util.Date;

public interface BaseModel {
    default Date createTime() {
        return new Date();
    }

    default Date updateTime() {
        return createTime();
    }
}
