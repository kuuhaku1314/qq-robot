package com.kuuhaku.robot.handler.unity;

import com.kuuhaku.robot.common.annotation.HandlerComponent;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.stereotype.Component;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/14 17:52
 * @Description 成员加群
 */
@HandlerComponent
@Slf4j
public class MemberJoinHandler {

    public void doHandler(MemberJoinEvent event) {
        if (event instanceof MemberJoinEvent.Invite) {
            MemberJoinEvent.Invite invite = (MemberJoinEvent.Invite) event;
            long invitorId = invite.getInvitor().getId();
            long memberId = invite.getMember().getId();
            MessageChain messageChain = MessageUtils.newChain();
            At invitorAt = new At(invitorId);
            At memberAt = new At(memberId);
            messageChain = messageChain.plus(memberAt).plus("进群了");
            messageChain = messageChain.plus("邀请者是").plus(invitorAt);
            event.getGroup().sendMessage(messageChain);
            return;
        }
        if (event instanceof MemberJoinEvent.Active) {
            MemberJoinEvent.Active active = (MemberJoinEvent.Active) event;
            long memberId = active.getMember().getId();
            At memberAt = new At(memberId);
            MessageChain messageChain = MessageUtils.newChain();
            messageChain = messageChain.plus(memberAt).plus("欢迎进群");
            event.getGroup().sendMessage(messageChain);
        }
    }

}
