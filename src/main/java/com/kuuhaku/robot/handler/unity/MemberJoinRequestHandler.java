package com.kuuhaku.robot.handler.unity;

import com.kuuhaku.robot.common.annotation.HandlerComponent;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;

/**
 * @Author by kuuhaku
 * @Date 2021/2/14 17:53
 * @Description 入群请求事件，只有机器人是管理才能收到
 */
@HandlerComponent
@Slf4j
public class MemberJoinRequestHandler {

    public void doHandler(MemberJoinRequestEvent event) {
        long fromId = event.getFromId();
        String message = event.getMessage();
        MessageChain messageChain = MessageUtils.newChain();
        messageChain = messageChain.plus(fromId + "").plus("申请入群").plus("\n");
        messageChain = messageChain.plus("他的入群宣言是:").plus(message);
        Group group = event.getGroup();
        if (group != null) {
            group.sendMessage(messageChain);
        }
    }

}
