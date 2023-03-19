package com.kuuhaku.robot.entity;

import lombok.Data;

/**
 * @author by kuuhaku
 * @date 2022/3/5 15:10
 * @description
 */
@Data
public class Illust {
    private long Pid;
    private String Title;
    private String Caption;
    private String Tags;
    private String ImageUrls;
    private String AgeLimit;
    private String CreatedTime;
    private long UserId;
    private String UserName;
}