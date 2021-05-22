package com.kuuhaku.robot.service;

import com.kuuhaku.robot.utils.RandomUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 3:16
 * @Description 复读service
 */
@Service
public class RepeatService {
    private final Map<String, String> map = new ConcurrentHashMap<>();
    /**
     * 排除那些词不进行复读
     */
    private static final Set<String> KEYWORDS = new HashSet<>();
    static {
        KEYWORDS.add("[图片]");
        KEYWORDS.add("[语音消息]");
        KEYWORDS.add("[闪照]");
        KEYWORDS.add("[戳一戳]");
        KEYWORDS.add("dd");
        KEYWORDS.add("涩图");
    }

    public void tryRepeat(MessageEvent event) {
        String content = event.getMessage().contentToString();
        // 关键词不复读
        if (KEYWORDS.contains(content)) {
            return;
        }
        String groupId = event.getSubject().getId() + "";
        String s = map.get(groupId);
        if (s != null && s.equals(content)) {
            if (RandomUtil.isPass(30)) {
                MessageChain result = MessageUtils.newChain();
                result = result.plus(content);
                event.getSubject().sendMessage(result);
            }
        } else {
            map.put(groupId, content);
        }
    }
}
