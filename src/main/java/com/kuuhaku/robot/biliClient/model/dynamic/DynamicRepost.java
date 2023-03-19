package com.kuuhaku.robot.biliClient.model.dynamic;

import com.kuuhaku.robot.biliClient.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
// 转发动态，是可以嵌套的
public class DynamicRepost implements BaseModel {
    // 本人的评论
    private String content;

    private Long timestamp;

    private Dynamic dynamic;
}
