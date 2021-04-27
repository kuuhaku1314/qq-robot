package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.VoiceService;
import com.kuuhaku.robot.utils.RandomUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Voice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/15 0:59
 * @Description 发送语音
 */
@HandlerComponent
public class VoiceHandler {

    public static List<String> ddVoicePaths;

    @Value("${robot.dd.path}")
    public String ddPath;
    @Value("${robot.koi.path}")
    public String koiPath;
    @Value("${robot.koi.two.path}")
    public String koiTwoPath;
    @Value("${robot.aqua.voice.path}")
    public String aquaVoicePath;

    @Autowired
    private VoiceService voiceService;

    @PostConstruct
    void init() {
        ddVoicePaths = DownloadService.getAmrFiles(ddPath);
    }

    @Permission
    @Handler(values = {"dd", "button"}, types = {HandlerMatchType.CONTAINS,
            HandlerMatchType.CONTAINS})
    public void sendButtonVoice(ChannelContext ctx) {
        Voice voice = voiceService.uploadVoice(
                ddVoicePaths.get(RandomUtil.random(ddVoicePaths.size())), (Group) ctx.group());
        ctx.group().sendMessage(voiceService.parseMsgChainByVoice(voice));
    }

    @Permission
    @Handler(values = {"恋口上"}, types = {HandlerMatchType.CONTAINS})
    public void sendKoiVoice(ChannelContext ctx) {
        Voice voice;
        if (RandomUtil.isPass(50)) {
            voice = voiceService.uploadVoice(koiPath, (Group) ctx.group());
        } else {
            voice = voiceService.uploadVoice(koiTwoPath, (Group) ctx.group());
        }
        ctx.group().sendMessage(voiceService.parseMsgChainByVoice(voice));
    }

    @Permission
    @Handler(values = {"阿夸语音"}, types = {HandlerMatchType.COMPLETE})
    public void sendAquaVoice(ChannelContext ctx) {
        Voice voice = voiceService.uploadVoice(aquaVoicePath, (Group) ctx.group());
        ctx.group().sendMessage(voiceService.parseMsgChainByVoice(voice));
    }

}
