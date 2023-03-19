package com.kuuhaku.robot.biliClient.api.relation;

import com.kuuhaku.robot.biliClient.BiliRequestFactor;
import com.kuuhaku.robot.biliClient.able.Listable;
import com.kuuhaku.robot.biliClient.model.relation.Relation;

public class RelationConditionImpl implements IRelationCondition {
    public Listable<Relation> toFollowers(long uid) {
        return new RelationList(BiliRequestFactor.getBiliRequest().setPath("/x/relation/followers")
                .setParams("vmid", Long.valueOf(uid)));
    }

    public Listable<Relation> toFollowings(long uid) {
        return new RelationList(BiliRequestFactor.getBiliRequest().setPath("/x/relation/followings")
                .setParams("vmid", Long.valueOf(uid)));
    }
}
