package com.kuuhaku.robot.common.aspect;

import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.PermissionRank;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 1:42
 * @Description 权限校验，针对messageEvent事件
 */

@Aspect
@Slf4j
@Component
@Order(10)
public class PermissionAspect {
    @Autowired
    private PermissionService permissionService;


    @Around("@annotation(permission)")
    public Object check(ProceedingJoinPoint pjp, Permission permission) {
        int level = permission.level();
        Object[] args = pjp.getArgs();
        ChannelContext context = (ChannelContext) args[0];
        MessageEvent event = context.event();
        int userLevel = getLevel(event);
        if (userLevel < level) {
            log.info("权限不足被拦截，指令发出人id=[{}]", event.getSender().getId());
            At at = new At(event.getSender().getId());
            MessageChain messageChain = MessageUtils.newChain();
            messageChain = messageChain.plus(at).plus(" 你没有相关权限");
            event.getSubject().sendMessage(messageChain);
            return Void.TYPE;
        }
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            log.info("权限切面发生异常", e);
            return Void.TYPE;
        }
    }

    private int getLevel(MessageEvent event) {
        long id = event.getSender().getId();
        if (permissionService.masterContains(id)) {
            return PermissionRank.MASTER;
        }
        if (permissionService.minusContains(id)) {
            return PermissionRank.MINUS;
        }
        if (permissionService.adminContains(id)) {
            return PermissionRank.ADMIN;
        }
        Group group = (Group) event.getSubject();
        NormalMember normalMember = group.get(id);
        if (normalMember == null) {
            return PermissionRank.MEMBER;
        }
        // 管理员或群主
        if (normalMember.getPermission().getLevel() != MemberPermission.MEMBER.getLevel()) {
            return PermissionRank.ADMIN;
        }
        return PermissionRank.MEMBER;
    }
}
