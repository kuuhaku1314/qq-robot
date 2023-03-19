package com.kuuhaku.robot.biliClient.api.dynamic;

import com.kuuhaku.robot.biliClient.BiliCondition;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.able.Listable;
import com.kuuhaku.robot.biliClient.model.dynamic.Dynamic;
import com.kuuhaku.robot.biliClient.model.dynamic.DynamicRoot;

public interface IDynamicCondition extends BiliCondition {
    Gettable<Dynamic> withDynamicId(Long paramLong);

    Listable<DynamicRoot> withHostUid(Long paramLong);
}
