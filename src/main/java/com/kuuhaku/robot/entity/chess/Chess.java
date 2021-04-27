package com.kuuhaku.robot.entity.chess;

import lombok.Data;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/11 7:19
 * @Description 井字棋子
 */
@Data
public class Chess {
    private String name;
    private int x;
    private int y;
    private String user;

    public Chess(String name, int x, int y, String user) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.user = user;
    }

    @Override
    public String toString() {
        return name;
    }


}
