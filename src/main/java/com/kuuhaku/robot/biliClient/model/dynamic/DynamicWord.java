package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author by kuuhaku
 * @date 2022/5/26 23:25
 * @description
 */
@Getter
@Setter
@ToString
// 文字动态
public class DynamicWord {
    private long rp_id;
    private long uid;
    // 文字内容
    private String content;
    private String ctrl;
    private int orig_dy_id;
    private int pre_dy_id;
    private long timestamp;
    private int reply;
}
