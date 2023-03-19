package com.kuuhaku.robot.common.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @Author by kuuhaku
 * @Date 2021/2/12 2:01
 * @Description handler组件类注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Service
public @interface HandlerComponent {
    String value() default "";
}
