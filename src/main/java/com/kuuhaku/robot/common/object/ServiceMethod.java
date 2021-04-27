package com.kuuhaku.robot.common.object;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 1:26
 * @Description 封装ServiceBean及handler方法，用于反射调用
 */
@Data
public class ServiceMethod {

    private Object serviceObj;
    private Method handlerMethod;
    private int order;

    public ServiceMethod(Object serviceObj, Method handlerMethod, int order) {
        this.serviceObj = serviceObj;
        this.handlerMethod = handlerMethod;
        this.order = order;
    }
}
