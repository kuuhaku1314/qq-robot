package com.kuuhaku.robot.handler.unity;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.TimeTaskService;
import com.kuuhaku.robot.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @Author by kuuhaku
 * @Date 2021/2/12 22:36
 * @Description 发送撤回消息
 */
@HandlerComponent
@Slf4j
public class MessageRecallHandler {
    private static final String RECALL = "recall:";

    @Autowired
    private RecordService recordService;

    @Autowired
    private TimeTaskService timeTaskService;


    public void doHandler(MessageRecallEvent.GroupRecall event) {
        long authorId = event.getAuthorId();
        At authorAt = new At(authorId);
        MessageChain messageChain = MessageUtils.newChain();
        Member operator = event.getOperator();
        if (operator == null || operator.getId() == event.getBot().getId()) {
            messageChain = messageChain.plus(authorAt).plus(" 的消息被撤回，操作人竟然是机器人");
        } else if (operator.getId() == authorId) {
            messageChain = messageChain.plus(authorAt).plus(" 偷偷撤回了一条消息，已记录，2分钟后重新发送，如需要停止发送，").
                    plus("请发送[取消发送已撤回消息]");
            String messageIds = intsToString(event.getMessageIds());
            Runnable runnable = () -> {
                MessageChain message = recordService.getMessage(event, messageIds);
                if (message != null) {
                    log.info("发送撤回的消息:" + message.contentToString());
                    event.getGroup().sendMessage(message);
                    message = MessageUtils.newChain();
                    message = message.plus("发送者为:").plus(authorAt);
                } else {
                    message = MessageUtils.newChain();
                    message = message.plus("数据被清除，故无法发送");
                }
                event.getGroup().sendMessage(message);
            };
            timeTaskService.submitTask(runnable, RECALL + event.getGroup().getId() + ":" + authorId, 2 * 60, TimeUnit.SECONDS);
        } else {
            At operatorAt = new At(operator.getId());
            messageChain = messageChain.plus(authorAt).plus("的消息被 ").plus(operatorAt).plus("撤回了");
        }
        event.getGroup().sendMessage(messageChain);
    }

    @Permission
    @Handler(values = {"取消发送已撤回消息"}, types = {HandlerMatchType.COMPLETE}, description = "让机器人不发送你撤回的消息")
    public void cancelSend(ChannelContext context) {
        boolean flag = timeTaskService.cancelTask(RECALL + context.groupId() + ":" + context.senderId());
        if (flag) {
            At at = new At(context.senderId());
            MessageChain messageChain = MessageUtils.newChain();
            messageChain = messageChain.plus(at).plus("\n").plus("取消发送成功");
            context.group().sendMessage(messageChain);
        }
    }

    private String intsToString(int[] ints) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i : ints) {
            stringBuilder.append(i);
        }
        return stringBuilder.toString();
    }

}
