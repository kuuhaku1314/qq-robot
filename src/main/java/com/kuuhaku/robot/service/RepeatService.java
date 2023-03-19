package com.kuuhaku.robot.service;

import com.kuuhaku.robot.utils.RandomUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 3:16
 * @Description 复读service
 */
@Service
public class RepeatService {
    private final Map<Long, String> map = new ConcurrentHashMap<>();
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
        KEYWORDS.add("[动画表情]");
        KEYWORDS.add("@");
        KEYWORDS.add("运势");
        KEYWORDS.add("叫号");
        KEYWORDS.add("人生重开");
        KEYWORDS.add("塔罗牌占卜");
        KEYWORDS.add("抽签");
        KEYWORDS.add("转发消息");
        KEYWORDS.add("p图");
        KEYWORDS.add("Y");
        KEYWORDS.add("N");
        KEYWORDS.add("DK");
        KEYWORDS.add("P");
        KEYWORDS.add("PN");
        KEYWORDS.add("y");
        KEYWORDS.add("n");
        KEYWORDS.add("dk");
        KEYWORDS.add("p");
        KEYWORDS.add("pn");
    }

    public void tryRepeat(MessageEvent event) {
        String content = event.getMessage().contentToString();
        // 关键词不复读
        for (String keyword : KEYWORDS) {
            if (content.contains(keyword)) {
                return;
            }
        }
        var groupId = event.getSubject().getId();
        String msg = map.get(groupId);
        if (msg != null && msg.equals(content)) {
            if (RandomUtil.isPass(20)) {
                event.getSubject().sendMessage(content);
            }
        } else {
            map.put(groupId, content);
        }
    }

}
