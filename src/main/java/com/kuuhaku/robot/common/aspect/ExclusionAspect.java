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
import java.util.Optional;

/**
 * @Author by kuuhaku
 * @Date 2021/2/15 16:58
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
    public Object around(ProceedingJoinPoint pjp, HandlerComponent handlerComponent) {
        Object[] args = pjp.getArgs();
        if (!enableExecute(args)) {
            return Void.TYPE;
        }
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            log.info("切面发生异常", e);
            return Void.TYPE;
        }
    }

    // 用于权限控制，某人是否被禁止使用，某群是否可以执行指令
    private boolean enableExecute(Object[] args) {
        if (args.length != 1) {
            return false;
        }
        var isMuted = false;
        var senderID = 0L;
        var groupID = 0L;
        if (args[0] instanceof ChannelContext context) {
            MessageEvent event = context.event();
            if (!permissionService.masterContains(event.getSender().getId())) {
                isMuted = ((Group) event.getSubject()).getBotAsMember().isMuted();
                senderID = event.getSender().getId();
                groupID = event.getSubject().getId();

            }
        } else if (args[0] instanceof MemberJoinEvent event) {
            isMuted = event.getGroup().getBotAsMember().isMuted();
            senderID = event.getUser().getId();
            groupID = event.getGroup().getId();
        } else if (args[0] instanceof MemberJoinRequestEvent event) {
            isMuted = Objects.requireNonNull(event.getGroup()).getBotAsMember().isMuted();
            senderID = Optional.ofNullable(event.getInvitorId()).orElse(0L);
            groupID = event.getGroup().getId();
        } else if (args[0] instanceof MemberLeaveEvent event) {
            isMuted = event.getGroup().getBotAsMember().isMuted();
            senderID = event.getUser().getId();
            groupID = event.getGroup().getId();
        } else if (args[0] instanceof MessageRecallEvent.GroupRecall event) {
            isMuted = event.getGroup().getBotAsMember().isMuted();
            senderID = event.getAuthorId();
            groupID = event.getGroup().getId();
        } else if (args[0] instanceof NudgeEvent event) {
            isMuted = ((Group) event.getSubject()).getBotAsMember().isMuted();
            senderID = event.getFrom().getId();
            groupID = event.getSubject().getId();
        }
        if (permissionService.masterContains(senderID)) {
            return true;
        }
        return !isMuted && !permissionService.exclusionGroupContains(groupID);
    }
}
