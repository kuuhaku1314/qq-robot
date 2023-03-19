package com.kuuhaku.robot.biliClient.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Vip {
    private Integer type;

    private Integer status;

    private Integer theme_type;

    private Label label;

    private Integer avatar_subscript;

    private String nickname_color;

    @ToString
    @Getter
    @Setter
    public static class Label {
        private String path;

        private String text;

        private String label_theme;
    }
}
