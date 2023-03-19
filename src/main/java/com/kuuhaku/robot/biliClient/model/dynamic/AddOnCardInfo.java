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
public class AddOnCardInfo {
    private Integer add_on_card_show_type;
    private ReserveAttachCard reserve_attach_card;
}
