package com.kuuhaku.robot.core.chain;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;


/**
 * @author by kuuhaku
 * @Date 2021/4/23 23:40
 * @Description
 */
public class ChannelContext {

    private final Command command;

    private final MessageEvent event;


    public ChannelContext(Command command, MessageEvent event) {
        this.command = command;
        this.event = event;
    }


    public Command command() {
        return command;
    }

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

    public MessageEvent event() {
        return event;
    }


}
