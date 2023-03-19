package com.kuuhaku.robot.biliClient.api.relation;

import com.kuuhaku.robot.biliClient.BiliCondition;
import com.kuuhaku.robot.biliClient.able.Listable;
import com.kuuhaku.robot.biliClient.model.relation.Relation;

public interface IRelationCondition extends BiliCondition {
    Listable<Relation> toFollowers(long paramLong);

    Listable<Relation> toFollowings(long paramLong);
}
