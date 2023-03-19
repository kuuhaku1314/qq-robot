package com.kuuhaku.robot.handler.unity;

import com.kuuhaku.robot.common.annotation.HandlerComponent;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;

/**
 * @Author by kuuhaku
 * @Date 2021/2/14 17:53
 * @Description 用户离开群
 */
@HandlerComponent
@Slf4j
public class MemberLeaveHandler {

    public void doHandler(MemberLeaveEvent event) {
        if (event instanceof MemberLeaveEvent.Kick) {
            MemberLeaveEvent.Kick kick = (MemberLeaveEvent.Kick) event;
            NormalMember operator = kick.getOperator();
            MessageChain messageChain = MessageUtils.newChain();
            if (operator != null) {
                long operatorId = operator.getId();
                long memberId = kick.getMember().getId();
                At at = new At(operatorId);
                messageChain = messageChain.plus(memberId + "").plus("被踢了").plus("\n");
                messageChain = messageChain.plus("操作者是").plus(at);
            } else {
                long memberId = kick.getMember().getId();
                messageChain = messageChain.plus(memberId + "").plus("被踢了").plus("\n");
                messageChain = messageChain.plus("操作者竟是我自己");
            }
            event.getGroup().sendMessage(messageChain);
            return;
        }
        if (event instanceof MemberLeaveEvent.Quit) {
            MemberLeaveEvent.Quit quit = (MemberLeaveEvent.Quit) event;
            long memberId = quit.getMember().getId();
            MessageChain messageChain = MessageUtils.newChain();
            messageChain = messageChain.plus(memberId + "").plus("跑路了");
            event.getGroup().sendMessage(messageChain);
        }
    }

}
