package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.chain.Command;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.ImageService;
import com.kuuhaku.robot.core.service.VoiceService;
import com.kuuhaku.robot.entity.music.NetEaseMusic;
import com.kuuhaku.robot.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 21:56
 * @Description 点歌
 */
@HandlerComponent
@Slf4j
public class MusicHandler {
    @Autowired
    private MusicService musicService;
    @Autowired
    private VoiceService voiceService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private ImageService imageService;

    private final Map<String, List<NetEaseMusic>> map = new ConcurrentHashMap<>();

    @Permission
    @Handler(values = {"点歌"}, types = {HandlerMatchType.START}, description = "格式如[点歌 那朵花]，回复[选择分享 序号]或[选择语音 序号]点歌")
    public void selectMusic(ChannelContext ctx) {
        // 获取歌名并检验
        Command command = ctx.command();
        if (command.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        command.params().forEach(sb::append);
        String musicName = sb.toString();
        // 检验该群当前是否有人正在点歌
        if (map.get(ctx.groupIdStr()) != null) {
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus("当前有人正在点歌").plus(at);
            ctx.group().sendMessage(result);
            return;
        }
        List<NetEaseMusic> musicList = musicService.getMusicList(musicName);
        // 获取歌单为空，说明未找到符合条件的歌曲
        if (musicList == null || musicList.isEmpty()) {
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus("未找到相关搜索结果").plus(at);
            ctx.group().sendMessage(result);
        } else {
            map.put(ctx.groupIdStr(), musicList);
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus(at);
            // 把歌单信息转换为图片，返回本地图片路径
            String musicListImagePath = musicService.getMusicListImagePath(musicList);
            if (musicListImagePath == null) {
                result = result.plus("点歌好像出了点问题");
                ctx.group().sendMessage(result);
            } else {
                Image image = imageService.uploadImage(musicListImagePath, ctx.event());
                MessageChain img = imageService.parseMsgChainByImg(image);
                ctx.group().sendMessage(result.plus("发送[选择分享 序号]或[选择语音 序号]点歌"));
                ctx.group().sendMessage(img);
                // 删除临时文件
                downloadService.deleteFile(musicListImagePath);
            }
        }
    }

    @Permission
    @Handler(values = {"选择分享"}, types = {HandlerMatchType.START}, description = "在点歌后发送[选择分享 1]，选择序号，会分享歌曲")
    public void shareMusicCard(ChannelContext ctx) {
        Command command = ctx.command();
        if (command.paramSize() != 1 || !StringUtils.isNumeric(command.params().get(0))) {
            return;
        }
        List<NetEaseMusic> netEaseMusics = map.get(ctx.groupIdStr());
        if (netEaseMusics == null) {
            return;
        }
        int index = Integer.parseInt(command.params().get(0)) - 1;
        if (index < 0 || index >= netEaseMusics.size()) {
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus("序号有误").plus(at);
            ctx.group().sendMessage(result);
            return;
        }
        NetEaseMusic easeMusic = netEaseMusics.get(index);
        MusicShare musicCard = musicService.getMusicCard(easeMusic);
        map.remove(ctx.groupIdStr());
        ctx.group().sendMessage(musicCard);
    }

    @Permission
    @Handler(values = {"选择语音"}, types = {HandlerMatchType.START}, description = "在点歌后发送[选择语音 1]，选择序号，会发送语音")
    public void shareMusicVoice(ChannelContext ctx) {
        Command command = ctx.command();
        if (command.paramSize() != 1 || !StringUtils.isNumeric(command.params().get(0))) {
            return;
        }
        List<NetEaseMusic> netEaseMusics = map.get(ctx.groupIdStr());
        if (netEaseMusics == null) {
            return;
        }
        int index = Integer.parseInt(command.params().get(0)) - 1;
        if (index < 0 || index >= netEaseMusics.size()) {
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus("序号有误").plus(at);
            ctx.group().sendMessage(result);
            return;
        }
        // 通过序号获取所点的歌曲信息
        NetEaseMusic easeMusic = netEaseMusics.get(index);
        // 通过歌曲信息，进行下载及格式的转换，返回转换后amr文件的本地路径
        String musicPath = musicService.getMusicPath(easeMusic);
        // 为vip歌曲时，无法下载，故路径为null
        if (musicPath == null) {
            MessageChain result = MessageUtils.newChain();
            result = result.plus("可能是vip或无版权歌曲，无法获取语音");
            ctx.group().sendMessage(result);
            return;
        }
        // 歌曲上传为语音
        Audio audio = voiceService.uploadAudio(musicPath, (Group) ctx.group());
        MessageChain voiceMessage = voiceService.parseMsgChainByAudio(audio);
        // 下载歌曲封面并上传
        String picturePath = downloadService.getRandomPngPath();
        downloadService.download(easeMusic.getPicUrl(), picturePath);
        Image image = imageService.uploadImage(picturePath, ctx.event());
        MessageChain imageMessage = imageService.parseMsgChainByImg(image);
        // 删除临时文件
        downloadService.deleteFile(musicPath);
        downloadService.deleteFile(picturePath);
        // 发送消息
        ctx.group().sendMessage(imageMessage);
        ctx.group().sendMessage(voiceMessage);
        // 移除点歌限制
        map.remove(ctx.groupIdStr());
    }

    @Permission
    @Handler(values = {"取消点歌"}, types = {HandlerMatchType.COMPLETE}, description = "点歌后取消点歌")
    public void cancelMusic(ChannelContext ctx) {
        List<NetEaseMusic> easeMusics = map.remove(ctx.groupIdStr());
        if (easeMusics != null) {
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus("取消成功").plus(at);
            ctx.group().sendMessage(result);
        }
    }

}
