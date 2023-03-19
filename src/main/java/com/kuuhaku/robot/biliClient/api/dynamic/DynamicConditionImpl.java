package com.kuuhaku.robot.biliClient.api.dynamic;

import com.kuuhaku.robot.biliClient.BiliRequestFactor;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.able.Listable;
import com.kuuhaku.robot.biliClient.model.dynamic.Dynamic;
import com.kuuhaku.robot.biliClient.model.dynamic.DynamicRoot;

public class DynamicConditionImpl implements IDynamicCondition {
    public Gettable<Dynamic> withDynamicId(Long dynamicId) {
        return new DynamicGet(BiliRequestFactor.getBiliRequest().useHostVC().setPath("/dynamic_svr/v1/dynamic_svr/get_dynamic_detail")
                .setParams("dynamic_id", dynamicId));
    }

    public Listable<DynamicRoot> withHostUid(Long uid) {
        return new DynamicList(BiliRequestFactor.getBiliRequest().useHostVC().setPath("/dynamic_svr/v1/dynamic_svr/space_history")
                .setParams("host_uid", uid));
    }
}
