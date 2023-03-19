package com.kuuhaku.robot.core.chain;

import com.kuuhaku.robot.common.constant.HandlerMatchType;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author by kuuhaku
 * @Date 2021/4/23 23:30
 * @Description
 */
@Slf4j
public class CommandChannel implements Channel {

    private final String id;

    private final ServiceMethod serviceMethod;

    private final int type;

    public CommandChannel(String id, ServiceMethod serviceMethod, int type) {
        this.id = id;
        this.serviceMethod = serviceMethod;
        this.type = type;
    }

    @Override
    public boolean execute(ChannelContext context) {
        Command command = context.command();
        if (isMatch(command.getMsg())) {
            Object serviceObj = serviceMethod.getServiceObj();
            Method handlerMethod = serviceMethod.getHandlerMethod();
            try {
                handlerMethod.invoke(serviceObj, context);
            } catch (Exception e) {
                log.error("反射调用发生异常");
                e.printStackTrace();
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public int order() {
        return serviceMethod.getOrder();
    }

    @Override
    public String description() {
        return serviceMethod.getDescription();
    }

    public int type() {
        return type;
    }

    private boolean isMatch(String msg) {
        switch (type) {
            case HandlerMatchType.START:
                return msg.startsWith(id);
            case HandlerMatchType.CONTAINS:
                return msg.contains(id);
            case HandlerMatchType.COMPLETE:
                return msg.equals(id);
            case HandlerMatchType.END:
                return msg.endsWith(id);
            default:
                return false;
        }
    }
}
