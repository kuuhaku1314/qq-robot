package com.kuuhaku.robot.biliClient;

import com.kuuhaku.robot.biliClient.api.dynamic.DynamicConditionImpl;
import com.kuuhaku.robot.biliClient.api.dynamic.IDynamicCondition;
import com.kuuhaku.robot.biliClient.api.relation.IRelationCondition;
import com.kuuhaku.robot.biliClient.api.relation.RelationConditionImpl;
import com.kuuhaku.robot.biliClient.api.user.IUserCondition;
import com.kuuhaku.robot.biliClient.api.user.UserConditionImpl;
import com.kuuhaku.robot.biliClient.api.video.IVideoCondition;
import com.kuuhaku.robot.biliClient.api.video.VideoConditionImpl;

public class BiliClientFactor {
    public static BiliClient getClient() {
        return new BiliClientDefault();
    }

    private static class BiliClientDefault implements BiliClient {
        private BiliClientDefault() {
        }

        public IUserCondition user() {
            return new UserConditionImpl();
        }

        public IDynamicCondition dynamic() {
            return new DynamicConditionImpl();
        }

        public IRelationCondition relation() {
            return new RelationConditionImpl();
        }

        public IVideoCondition video() {
            return new VideoConditionImpl();
        }
    }
}
