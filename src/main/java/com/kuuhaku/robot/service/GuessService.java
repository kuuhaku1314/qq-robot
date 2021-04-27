package com.kuuhaku.robot.service;

import com.kuuhaku.robot.utils.HttpsUtil;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 5:29
 * @Description 用于猜题
 */
public class GuessService {


    public void guess() {
    }

    public static void main(String[] args) {
        try {
            HttpsUtil.doGet("https://www.baidu.com/", new Proxy(Proxy.Type.HTTP, new InetSocketAddress("49.234.94.172", 8118)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
