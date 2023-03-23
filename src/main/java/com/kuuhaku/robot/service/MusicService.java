package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.entity.music.NetEaseMusic;
import com.kuuhaku.robot.service.musicApi.NetEaseMusicApi;
import com.kuuhaku.robot.utils.MojiUtil;
import com.kuuhaku.robot.utils.VoiceUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MusicShare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;


/**
 * @Author by kuuhaku
 * @Date 2021/2/13 21:13
 * @Description 网易云音乐相关
 */
@Service
@Slf4j
public class MusicService {
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private NetEaseMusicApi netEaseMusicApi;

    public List<NetEaseMusic> getMusicList(String musicName) {
        return netEaseMusicApi.getNetEaseMusicPage(musicName);
    }

    /**
     * 文字分享版本
     *
     * @param list
     * @param messageChain
     * @return
     */
    public MessageChain getMusicListMessage(List<NetEaseMusic> list, MessageChain messageChain) {
        messageChain = messageChain.plus("我找到了这些~!").plus("\n").plus("=== 网易云音乐 ===").plus("\n");
        for (int i = 0; i < list.size(); i++) {
            messageChain = messageChain.plus("" + (i + 1) + ". ").plus(list.get(i).getName() + " - ");
            List<String> artists = list.get(i).getArtists();
            if (!artists.isEmpty()) {
                messageChain = messageChain.plus(artists.get(0));
            }
            messageChain = messageChain.plus("\n");
        }
        return messageChain;
    }

    public String getMusicListImagePath(List<NetEaseMusic> list) {
        StringBuilder imageInfo = new StringBuilder("""
                我找到了这些~!
                === 网易云音乐 ===
                """);
        for (int i = 0; i < list.size(); i++) {
            imageInfo.append(i + 1).append(". ").append(list.get(i).getName()).append(" - ");
            List<String> artists = list.get(i).getArtists();
            imageInfo.append(String.join("/", artists));
            imageInfo.append("\n");
        }
        String imagePath = downloadService.getRandomPngPath();
        if (MojiUtil.createImage(imageInfo.toString(), imagePath)) {
            return imagePath;
        }
        return null;
    }


    public MusicShare getMusicCard(NetEaseMusic netEaseMusic) {
        return netEaseMusicApi.getMusicCard(netEaseMusic);
    }

    /**
     * 获取本地路径
     *
     * @param netEaseMusic
     * @return
     */
    public String getMusicPath(NetEaseMusic netEaseMusic) {
        String musicUrl = netEaseMusicApi.getMusicUrl(netEaseMusic);
        log.info("当前下载路径为:" + musicUrl);
        String mp3FileName = downloadService.getRandomPath() + ".mp3";
        String amrFileName = downloadService.getRandomPath() + ".amr";
        downloadService.download(musicUrl, mp3FileName);
        File source = new File(mp3FileName);
        // 为VIP歌曲下，文件为空
        if (source.length() <= 10L) {
            downloadService.deleteFile(mp3FileName);
            return null;
        }
        boolean isSuccess = VoiceUtil.transfer(mp3FileName, amrFileName);
        downloadService.deleteFile(mp3FileName);
        downloadService.deleteFile(amrFileName);
        if (isSuccess) {
            return amrFileName;
        } else {
            return null;
        }
    }
}
