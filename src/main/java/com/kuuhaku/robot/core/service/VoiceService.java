package com.kuuhaku.robot.core.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Audio;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 15:42
 * @Description 上传语音
 */
@Slf4j
@Service
public class VoiceService {

    public Audio uploadAudio(String localPath, Group group) {
        File file = new File(localPath);
        if (!file.exists()) {
            return null;
        }
        ExternalResource externalResource = ExternalResource.create(file);
        Audio audio = group.uploadAudio(externalResource);
        try {
            externalResource.inputStream().close();
            externalResource.close();
            log.info("关闭语音流成功");
            return audio;
        } catch (IOException e) {
            log.info("关闭语音流出现异常");
            e.printStackTrace();
            return audio;
        }
    }

    /**
     * 解释语音为message
     *
     * @param audio 语音
     * @return 语音消息
     */
    public MessageChain parseMsgChainByAudio(Audio audio) {
        MessageChain messageChain = MessageUtils.newChain();
        if (audio == null) {
            messageChain = messageChain.plus("发送语音失败");
        } else {
            messageChain = messageChain.plus(audio);
        }
        return messageChain;
    }
}
