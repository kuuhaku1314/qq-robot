package com.kuuhaku.robot.entity.othello;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author by kuuhaku
 * @date 2022/4/16 14:37
 * @description
 */
@Data
@AllArgsConstructor
public
class ChessOperation {
    private int type;
    private int x;
    private int y;
    private String userID;
}
