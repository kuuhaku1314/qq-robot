package com.kuuhaku.robot.biliClient;

import com.kuuhaku.robot.biliClient.model.dynamic.Dynamic;

import java.util.List;

/**
 * @author by kuuhaku
 * @date 2022/5/26 22:37
 * @description
 */
public class Main {
    public static void main(String[] args) {
        BiliClient client = BiliClientFactor.getClient();
        List<Dynamic> items = client.dynamic().withHostUid(380829248L).list().getItems();
        items.forEach(System.out::println);
        // http://api.bilibili.com/x/space/acc/info?mid=11783021
        // http://api.vc.bilibili.com/dynamic_svr/v100/dynamic_svr/space_history?host_uid=11783021
    }
}
