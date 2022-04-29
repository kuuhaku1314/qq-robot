package com.kuuhaku.robot.entity;

import lombok.Data;

import java.util.List;

/**
 * @author by kuuhaku
 * @date 2022/3/5 18:35
 * @description
 */
@Data
public class Quiz {
    private String title;
    private List<String> answers;
}
