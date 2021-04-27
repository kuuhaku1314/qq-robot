package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/16 21:18
 * @Description 转发消息
 */
@HandlerComponent
@Slf4j
public class ForwardHandler {

    @Permission()
    @Handler(values = {"假消息"}, types = {HandlerMatchType.START})
    public void toRecord(ChannelContext ctx) {
        List<Pair<String, String>> pairList = getParam(ctx.command().params());
        if (pairList == null || pairList.isEmpty()) {
            log.info("构造假消息参数不对");
            return;
        }
        List<ForwardMessage.Node> list = new ArrayList<>();
        int timeMillis = (int) (System.currentTimeMillis() / 1000);
        for (Pair<String, String> pair : pairList) {
            long id = Long.parseLong(pair.getKey());
            String content = pair.getValue();
            Group group = (Group) ctx.group();
            MessageChain messageChain = MessageUtils.newChain();
            messageChain = messageChain.plus(content);
            ForwardMessage.Node node = new ForwardMessage.Node(id, timeMillis, group.get(id).getNick(), messageChain);
            list.add(node);
            timeMillis = timeMillis + 10;
        }
        List<String> previewList = new ArrayList<>();
        int num = 0;
        for (ForwardMessage.Node node : list) {
            if (num < 3) {
                previewList.add(node.getSenderName() + ":" + node.getMessageChain().contentToString());
                num++;
            } else {
                break;
            }
        }
        String title = "群聊的聊天记录";
        String brief = "[聊天记录]";
        String source = "聊天记录";
        String summary = "查看转发消息";
        ForwardMessage forwardMessage = new ForwardMessage(previewList, title, brief, source, summary, list);
        ctx.group().sendMessage(forwardMessage);
        log.info("构造发送假消息成功");
    }

    private List<Pair<String, String>> getParam(List<String> params) {
        List<Pair<String, String>> pairList = new ArrayList<>();
        if (params.size() % 2 != 0) {
            return pairList;
        }
        for (int i = 0; i < params.size(); i = i + 2) {
            if (params.get(i).charAt(0) != '@') {
                return null;
            }
            String id = params.get(i).substring(1);
            Pair<String, String> stringStringPair = new ImmutablePair<>(id, params.get(i + 1));
            pairList.add(stringStringPair);
        }
        return pairList;
    }

}
