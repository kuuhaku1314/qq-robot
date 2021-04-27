package com.kuuhaku.robot.entity.othello;

import lombok.Data;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/11 16:37
 * @Description 创建对局，用于等待多人进入游戏时一些检测处理
 */
@Data
public class OthelloGroup {

    private String groupId;
    private String white;
    private String black;
    private String whiteNick;
    private String blackNick;
}
