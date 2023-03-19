package com.kuuhaku.robot.core.chain;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;


/**
 * @author by kuuhaku
 * @Date 2021/4/23 23:40
 * @Description
 */
public record ChannelContext(Command command,
                             MessageEvent event) {

    public Command reverseCommand() {
        return command.reverseCommand();
    }

    public long senderId() {
        return event.getSender().getId();
    }

    public String senderIdStr() {
        return event.getSender().getId() + "";
    }

    public long groupId() {
        return event.getSubject().getId();
    }

    public String groupIdStr() {
        return event.getSubject().getId() + "";
    }

    public Contact group() {
        return event.getSubject();
    }

    public User sender() {
        return event.getSender();
    }

    public String nickname() {
        return event.getSender().getNick();
    }


}
