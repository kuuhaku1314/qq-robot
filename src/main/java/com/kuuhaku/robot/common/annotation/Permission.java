package com.kuuhaku.robot.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 1:48
 * @Description 权限要求等级
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    int level() default 0;
}
