package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author by kuuhaku
 * @date 2022/5/27 2:25
 * @description
 */
@Getter
@Setter
@ToString
public class ReserveAttachCard {
    private String type;
    // 直播预约：猫猫と雑多！！！！
    private String title;

    // 490人预约
    private String desc_second;
    // 直播开始时间
    private Long livePlanStartTime;

    private DescFirst desc_first;
}
