package com.kuuhaku.robot.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author by kuuhaku
 * @date 2022/1/3 14:16
 * @description
 */
public class CmdUtil {

    /**
     * 返回cmd的输出流
     *
     * @param cmd 命令
     * @return 输出流，以行划分
     */
    public static List<String> executeCmd(String cmd) {
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), "GBK"));
            String line;
            List<String> list = new ArrayList<>();
            while ((line = in.readLine()) != null) {
                list.add(line);
            }
            in.close();
            proc.waitFor();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}