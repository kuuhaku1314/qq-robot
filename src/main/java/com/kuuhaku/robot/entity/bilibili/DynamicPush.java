package com.kuuhaku.robot.entity.bilibili;

import com.kuuhaku.robot.biliClient.model.dynamic.Dynamic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * @author by kuuhaku
 * @date 2022/5/24 20:32
 * @description
 */
@Setter
@Getter
@AllArgsConstructor
@ToString
public class DynamicPush {
    private Long uid;
    private Dynamic dynamic;
    private Set<Long> groups;
    private int retryCount;
}
