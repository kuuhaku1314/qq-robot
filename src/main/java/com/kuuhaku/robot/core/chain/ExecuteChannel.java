package com.kuuhaku.robot.core.chain;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author by kuuhaku
 * @Date 2021/4/24 10:09
 * @Description
 */
@Slf4j
public class ExecuteChannel implements Channel{

    private final String id;

    private final ServiceMethod serviceMethod;

    public ExecuteChannel(String id, ServiceMethod serviceMethod) {
        this.id = id;
        this.serviceMethod = serviceMethod;
    }

    @Override
    public boolean execute(ChannelContext context) {
        Object serviceObj = serviceMethod.getServiceObj();
        Method handlerMethod = serviceMethod.getHandlerMethod();
        try {
            handlerMethod.invoke(serviceObj, context);
        } catch (Exception e) {
            log.error("反射调用发生异常");
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public int order() {
        return serviceMethod.getOrder();
    }

}
