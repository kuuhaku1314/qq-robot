package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.chain.Command;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.service.ImageApiService;
import com.kuuhaku.robot.core.service.ImageService;
import com.kuuhaku.robot.core.service.TaskService;
import com.kuuhaku.robot.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 2:04
 * @Description 发送图片
 */
@HandlerComponent
@Slf4j
public class SendImageHandler {
    public static List<String> aquaImagePaths;
    public static List<String> aliceImagePaths;
    public static List<String> mangaFolderPaths;
    @Value("${robot.aqua.path}")
    public String aquaPath;
    @Value("${robot.alice.path}")
    public String alicePath;
    @Value("${robot.manga.path}")
    public String mangaPath;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private ImageApiService imageApiService;
    @Autowired
    private DownloadService downloadService;

    @PostConstruct
    void init() {
        aquaImagePaths = DownloadService.getImageFiles(aquaPath);
        aliceImagePaths = DownloadService.getImageFiles(alicePath);
        mangaFolderPaths = DownloadService.getFolder(mangaPath);
    }

    @Permission
    @Handler(values = {"夸", "阿夸", "aqua", "夸图"}, types = {HandlerMatchType.START,
            HandlerMatchType.CONTAINS, HandlerMatchType.CONTAINS, HandlerMatchType.CONTAINS})
    public void sendAqua(ChannelContext ctx) {
        int times = ctx.command().isEmpty() ? 1 : RandomUtil.getTimes(ctx.command().params().get(0), 1, 5);
        int[] ints = new Random(System.currentTimeMillis()).ints(0, aquaImagePaths.size()).limit(times).toArray();
        for (int i : ints) {
            taskService.submitTask(() -> {
                Image image = imageService.uploadImage(aquaImagePaths.get(i), ctx.event());
                MessageChain result = imageService.parseMsgChainByImg(image);
                ctx.group().sendMessage(result);
            });
        }
    }

    @Permission
    @Handler(values = {"alice", "爱丽丝", "Alice"}, types = {HandlerMatchType.CONTAINS,
            HandlerMatchType.CONTAINS, HandlerMatchType.CONTAINS})
    public void sendAlice(ChannelContext ctx) {
        int times = ctx.command().isEmpty() ? 1 : RandomUtil.getTimes(ctx.command().params().get(0), 1, 5);
        int[] ints = new Random(System.currentTimeMillis()).ints(0, aliceImagePaths.size()).limit(times).toArray();
        for (int i : ints) {
            taskService.submitTask(() -> {
                Image image = imageService.uploadImage(aliceImagePaths.get(i), ctx.event());
                MessageChain result = imageService.parseMsgChainByImg(image);
                ctx.group().sendMessage(result);
            });
        }
    }

    @Permission
    @Handler(values = {"发本子"}, types = {HandlerMatchType.END})
    public void sendManga(ChannelContext ctx) {
        Command command = ctx.reverseCommand();
        if (command.isEmpty()) {
            return;
        }
        String id = command.params().get(0).substring(1);
        if (!StringUtils.isNumeric(id)) {
            return;
        }
        Group group = (Group) ctx.group();
        NormalMember normalMember = group.get(Long.parseLong(id));
        if (normalMember == null) {
            return;
        }
        log.info("开始发本了");
        long userId = normalMember.getId();
        String nickname = normalMember.getNick();
        ArrayList<String> imageFiles = DownloadService.getImageFiles(
                mangaFolderPaths.get(RandomUtil.random(mangaFolderPaths.size())));
        int timeMillis = (int) (System.currentTimeMillis() / 1000);
        List<ForwardMessage.Node> list = new ArrayList<>();
        MessageChain messageChain = MessageUtils.newChain();
        for (String imagePath : imageFiles) {
            Image image = imageService.uploadImage(imagePath, ctx.event());
            MessageChain imageMessage = imageService.parseMsgChainByImg(image);
            ForwardMessage.Node node = new ForwardMessage.Node(userId, timeMillis, nickname, imageMessage);
            list.add(node);
            timeMillis = timeMillis + 5;
        }
        messageChain = messageChain.plus("点个赞呗");
        ForwardMessage.Node endNode = new ForwardMessage.Node(userId, timeMillis, nickname, messageChain);
        list.add(endNode);
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

    @Permission
    @Handler(values = {"色图", "涩图"}, types = {HandlerMatchType.COMPLETE,
            HandlerMatchType.COMPLETE})
    public void sendBadImage(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        At at = new At(ctx.senderId());
        result = result.plus("你可少看点吧\uD83D\uDE4F").plus(at);
        ctx.group().sendMessage(result);
    }

    @Permission
    @Handler(values = {"来张色图", "来张涩图", "来点涩图", "来点色图"}, types = {HandlerMatchType.START,
            HandlerMatchType.START, HandlerMatchType.START, HandlerMatchType.START})
    public void sendGoodImage(ChannelContext ctx) {
        int times = ctx.command().isEmpty() ? 1 : RandomUtil.getTimes(ctx.command().params().get(0), 1, 5);
        for (int i = 0; i < times; i++) {
            taskService.submitTask(() -> {
                String path = imageApiService.randomImagePath();
                if (path == null) {
                    MessageChain result = MessageUtils.newChain();
                    At at = new At(ctx.senderId());
                    result = result.plus("尝试发五次还能失败，你太衰了\uD83D\uDE4F").plus(at);
                    ctx.group().sendMessage(result);
                    return;
                }
                Image image = imageService.uploadImage(path, ctx.event());
                MessageChain result = imageService.parseMsgChainByImg(image);
                downloadService.deleteFile(path);
                ctx.group().sendMessage(result);
            });
        }
    }

}
