package com.kuuhaku.robot.entity.chess;

import lombok.Data;

/**
 * @Author by kuuhaku
 * @Date 2021/2/11 11:10
 * @Description 创建对局，用于等待多人进入游戏时一些检测处理
 */
@Data
public class ChessGroup {

    private String groupId;
    private String red;
    private String black;
    private String redNick;
    private String blackNick;

}
