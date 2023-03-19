package com.kuuhaku.robot.common.annotation;

import com.kuuhaku.robot.common.constant.HandlerMatchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kuuhaku
 * @Author by kuuhaku
 * @Date 2021/2/12 1:15
 * @Description handler类的方法注解，用于路径方法匹配，指令及类型数目需要匹配，适用于messageEvent参数类型
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
    /**
     * 操作指令数组
     *
     * @return command
     */
    String[] values() default {""};

    /**
     * 对应操作匹配类型，具体看HandlerMatchType
     * {@link HandlerMatchType}
     *
     * @return 匹配类型
     */
    int[] types() default {HandlerMatchType.ALL};

    /**
     * 只要是消息就会执行的处理器的order
     * 执行顺序重小到大
     * 为负数的所有order都会执行
     * 为正数的根据方法返回状态决定是否要执行
     *
     * @return 处理顺序
     */
    int order() default 0;

    /**
     * 指令描述
     *
     * @return 指令描述
     */
    String description() default "";
}
