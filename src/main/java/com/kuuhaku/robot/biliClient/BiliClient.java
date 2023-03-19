package com.kuuhaku.robot.biliClient;

import com.kuuhaku.robot.biliClient.api.dynamic.IDynamicCondition;
import com.kuuhaku.robot.biliClient.api.relation.IRelationCondition;
import com.kuuhaku.robot.biliClient.api.user.IUserCondition;
import com.kuuhaku.robot.biliClient.api.video.IVideoCondition;

public interface BiliClient {
    IUserCondition user();

    IDynamicCondition dynamic();

    IRelationCondition relation();

    IVideoCondition video();
}
