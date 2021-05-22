package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.VoiceService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.Voice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 21:45
 * @Description 信息保存
 */
@Slf4j
@Service
public class RecordService {

    @Autowired
    private DownloadService downloadService;

    /**
     * key:groupId:messageIds
     */
    private Map<String, String> map = new ConcurrentHashMap<>();

    private static final int MAX_SIZE = 10000;

    @Value("${robot.temp.path}")
    public String voicePath;

    public static final String VOICE_KEY = "[mirai:voice]";

    @Autowired
    private VoiceService voiceService;

    public void record(String groupId, MessageChain messageChain) {
        String messageIds = getMessageIds(messageChain);
        if (map.size() > MAX_SIZE) {
            map = new HashMap<>(1000);
        }
        Voice voice = messageChain.get(Voice.Key);
        if (voice != null) {
            log.info("开始进行语音下载");
            String url = voice.getUrl();
            log.info("下载url为" + url);
            downloadService.download(url, getVoiceRealPath(groupId, messageIds));
            map.put(groupId + ":" + messageIds, VOICE_KEY);
            return;
        }
        map.put(groupId + ":" + messageIds, messageChain.serializeToMiraiCode());
    }


    /**
     * 获取消息的id
     * @param messageChain 消息
     * @return id
     */
    private String getMessageIds(MessageChain messageChain) {
        // 处理事件
        int start = 15;
        String messageIds = "";
        String message = messageChain.toString();
        for (int i = start; i < message.length(); i++) {
            if (message.charAt(i) == ']') {
                messageIds = message.substring(start, i);
                break;
            }
        }
        return messageIds;
    }

    /**
     * 撤回专用，针对语音做了处理
     * @param event 撤回事件
     * @param messageIds 撤回的消息id
     * @return 消息
     */
    public MessageChain getMessage(MessageRecallEvent.GroupRecall event, String messageIds) {
        String groupId = event.getGroup().getId() + "";
        String message = map.get(groupId + ":" + messageIds);
        if (message != null) {
            // 语音消息特殊处理
            log.info("message内容为[{}]", message);
            if (message.equals(VOICE_KEY)) {
                // 语音文件路径
                Voice voice = voiceService.uploadVoice(getVoiceRealPath(groupId, messageIds), event.getGroup());
                if (voice == null) {
                    return null;
                }
                return voiceService.parseMsgChainByVoice(voice);
            }
            return MiraiCode.deserializeMiraiCode(message);
        }
        return null;
    }


    /**
     * 获取保存的语音路径
     * @param groupId 群id
     * @param messageIds 消息id
     * @return
     */
    private String getVoiceRealPath(String groupId, String messageIds) {
        return voicePath + File.separator + groupId + "_" + messageIds + ".amr";
    }
}
