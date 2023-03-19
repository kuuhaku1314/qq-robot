package com.kuuhaku.robot.biliClient.api.dynamic;

import com.kuuhaku.robot.biliClient.BiliCall;
import com.kuuhaku.robot.biliClient.BiliRequest;
import com.kuuhaku.robot.biliClient.BiliResult;
import com.kuuhaku.robot.biliClient.able.Listable;
import com.kuuhaku.robot.biliClient.exception.BiliRequestException;
import com.kuuhaku.robot.biliClient.model.dynamic.DynamicRoot;

public class DynamicList implements Listable<DynamicRoot> {
    private final BiliRequest request;

    public DynamicList(BiliRequest request) {
        this.request = request;
    }

    public DynamicRoot list() {
        BiliResult biliResult = BiliCall.doCall(this.request);
        return DynamicRoot.build(biliResult);
    }

    @Deprecated
    public DynamicRoot list(Long limit, Long offset) {
        throw new BiliRequestException(this.request.getURI(), "目前不支持此种方式调用");
    }

    public DynamicRoot list(Long nextOffset) {
        BiliResult biliResult = BiliCall.doCall(this.request.setParams("offset_dynamic_id", nextOffset));
        return DynamicRoot.build(biliResult);
    }
}
