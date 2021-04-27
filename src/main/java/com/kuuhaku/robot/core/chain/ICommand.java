package com.kuuhaku.robot.core.chain;

import java.util.List;

/**
 * @author by kuuhaku
 * @Date 2021/4/23 22:44
 * @Description
 */
public interface ICommand {

    /**
     * 基础命令
     * @return 命令名称
     */
    String baseCommand();

    /**
     * 命令参数
     * @return 参数列表
     */
    List<String> params();

}
