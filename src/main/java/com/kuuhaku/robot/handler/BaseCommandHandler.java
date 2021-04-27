package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.ImageService;
import com.kuuhaku.robot.utils.MojiUtil;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 2:30
 * @Description 发送帮助信息
 */
@HandlerComponent
public class BaseCommandHandler {
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private ImageService imageService;
    public static String message;
    static {
        message = "本robot指令有" + "\n";
        message = message + "[阿夸]，[夸图]，[爱丽丝]，[alice]，发送[名字+空格+次数]可以发送多张图片，最多5张" + "\n";
        message = message + "某些关键词会触发发送网上随机获取的图片" + "\n";
        message = message + "[创建井字棋]，[加入井字棋]，[井字棋认输]，[取消井字棋]。同时另有一黑白棋指令与此类似。" + "\n";
        message = message + "[dd]，[button]，[恋口上]，发送dd语音" + "\n";
        message = message + "[点歌+空格+歌名]进行点歌，[我要点歌+空格+歌单的选择项]进行歌曲分享，[语音点歌+空格+歌单的选择项]进行语音发送分享，[取消点歌]" + "\n";
        message = message + "[骰子]发送一个随机骰子，[骰子+空格+点数]发送指定点数的骰子" + "\n";
        message = message + "[@某人+搓]，[@某人+爬],[@某人+丢]发送表情包" + "\n";
        message = message + "入群欢迎，出群提醒，有人加群提醒(需要管理员权限)" + "\n";
        message = message + "复读机功能，防撤回功能，对撤回消息进行2分钟后重新发送，可由本人@robot+取消发送进行取消" + "\n";
        message = message + "戳robot，robot会反戳或者发送[放大招]" + "\n";
        message = message + "某些关键词会触发禁言(需要管理员权限)" + "\n";
        message = message + "搜图功能(感觉qq显示出来也没啥用，暂时未添加)" + "\n";
        message = message + "发送群聊假消息" + "\n";
        message = message + "一些其他的功能";
    }

    @Permission
    @Handler(values = {"help", "帮助"}, types = {HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE})
    public void sendHelpInfo(ChannelContext ctx) {
        String path = downloadService.getRandomPngPath();
        String imagePath = MojiUtil.createImage(message, path);
        Image image = imageService.uploadImage(imagePath, ctx.event());
        MessageChain messageChain = imageService.parseMsgChainByImg(image);
        ctx.group().sendMessage(messageChain);
        downloadService.deleteFile(path);
    }
}
