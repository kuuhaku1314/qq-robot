package com.kuuhaku.robot.biliClient.model.relation;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.biliClient.BiliResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
public class Relation {
    private long total;

    private long re_version;

    private List<Follower> items;


    public static com.kuuhaku.robot.biliClient.model.relation.Relation build(BiliResult result) {
        com.kuuhaku.robot.biliClient.model.relation.Relation relation = result.toData(com.kuuhaku.robot.biliClient.model.relation.Relation.class);
        List<Follower> list = JSONObject.parseObject(result.getData().toString()).getJSONArray("list").toJavaList(Follower.class);
        relation.setItems(list);
        return relation;
    }
}
