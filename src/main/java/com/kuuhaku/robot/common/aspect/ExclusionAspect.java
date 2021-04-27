package com.kuuhaku.robot.common.aspect;

import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/15 16:58
 * @Description 排除指定group列表，不触发messageEvent事件
 */
@Aspect
@Component
@Slf4j
@Order(5)
public class ExclusionAspect {

    @Autowired
    private PermissionService permissionService;


    @Around("@within(handlerComponent)")
    public Object around(ProceedingJoinPoint pjp, HandlerComponent handlerComponent){
        Object[] args = pjp.getArgs();
        if (args.length == 1) {
            if (args[0] instanceof ChannelContext) {
                ChannelContext context = (ChannelContext) args[0];
                MessageEvent event = context.event();
                if (!permissionService.masterContains(event.getSender().getId())) {
                    if (permissionService.exclusionGroupContains(event.getSubject().getId()) ||
                            ((Group) event.getSubject()).getBotAsMember().isMuted()) {
                        return Void.TYPE;
                    }
                }
            } else if (args[0] instanceof MemberJoinEvent) {
                MemberJoinEvent event = (MemberJoinEvent) args[0];
                if (permissionService.exclusionGroupContains(event.getGroup().getId()) ||
                        (event.getGroup()).getBotAsMember().isMuted()) {
                    return Void.TYPE;
                }
            } else if (args[0] instanceof MemberJoinRequestEvent) {
                MemberJoinRequestEvent event = (MemberJoinRequestEvent) args[0];
                if (permissionService.exclusionGroupContains(Objects.requireNonNull(event.getGroup()).getId()) ||
                        (Objects.requireNonNull(event.getGroup())).getBotAsMember().isMuted()) {
                    return Void.TYPE;
                }
            } else if (args[0] instanceof MemberLeaveEvent) {
                MemberLeaveEvent event = (MemberLeaveEvent) args[0];
                if (permissionService.exclusionGroupContains(event.getGroup().getId()) ||
                        (event.getGroup()).getBotMuteRemaining() > 0) {
                    return Void.TYPE;
                }
            } else if (args[0] instanceof MessageRecallEvent.GroupRecall) {
                MessageRecallEvent.GroupRecall event = (MessageRecallEvent.GroupRecall) args[0];
                if (permissionService.exclusionGroupContains(event.getGroup().getId()) ||
                        (event.getGroup()).getBotAsMember().isMuted()) {
                    return Void.TYPE;
                }
            } else if (args[0] instanceof NudgeEvent) {
                NudgeEvent event = (NudgeEvent) args[0];
                if (permissionService.exclusionGroupContains(event.getSubject().getId()) ||
                        ((Group) event.getSubject()).getBotAsMember().isMuted()) {
                    return Void.TYPE;
                }
            }
        }
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            log.info("切面发生异常", e);
            return Void.TYPE;
        }
    }
}
