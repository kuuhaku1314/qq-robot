package com.kuuhaku.robot.biliClient.api.dynamic;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.biliClient.BiliCall;
import com.kuuhaku.robot.biliClient.BiliRequest;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.model.dynamic.Dynamic;
import com.kuuhaku.robot.biliClient.model.dynamic.DynamicRaw;

public class DynamicGet implements Gettable<Dynamic> {
    private final BiliRequest request;

    public DynamicGet(BiliRequest request) {
        this.request = request;
    }

    public Dynamic get() {
        Object data = BiliCall.doCall(this.request).getData();
        return JSONObject.parseObject(data.toString()).getJSONObject("card")
                .toJavaObject(DynamicRaw.class).toDynamic();
    }
}
