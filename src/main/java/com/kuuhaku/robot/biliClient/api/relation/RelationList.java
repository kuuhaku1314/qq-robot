package com.kuuhaku.robot.biliClient.api.relation;

import com.kuuhaku.robot.biliClient.BiliCall;
import com.kuuhaku.robot.biliClient.BiliRequest;
import com.kuuhaku.robot.biliClient.able.Listable;
import com.kuuhaku.robot.biliClient.model.relation.Relation;

public class RelationList implements Listable<Relation> {
    private final BiliRequest result;

    public RelationList(BiliRequest result) {
        this.result = result;
    }

    public Relation list() {
        return Relation.build(BiliCall.doCall(this.result));
    }

    public Relation list(Long limit, Long offset) {
        return Relation.build(BiliCall.doCall(this.result.setParams("pn", Long.valueOf(offset.longValue() / limit.longValue())).setParams("ps", limit)));
    }

    public Relation list(Long nextOffset) {
        return Relation.build(BiliCall.doCall(this.result.setParams("pn", nextOffset)));
    }

    public Relation listPage(Long size, Long page) {
        return Relation.build(BiliCall.doCall(this.result.setParams("pn", page).setParams("ps", size)));
    }
}
