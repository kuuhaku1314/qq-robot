package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author by kuuhaku
 * @date 2022/5/26 23:41
 * @description
 */
@Getter
@Setter
@ToString
// 文字/图片动态下附带的小卡片，比如直播框啥的
public class DynamicDisplay {
    private Object relation;
    private Object comment_info;
    private List<AddOnCardInfo> add_on_card_info;
    // 转发的可能有这个结构
    private DynamicDisplay origin;
}
