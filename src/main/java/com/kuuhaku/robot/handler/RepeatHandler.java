package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.common.constant.PermissionRank;
import com.kuuhaku.robot.config.ProxyConfig;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.entity.Quiz;
import com.kuuhaku.robot.service.AudioService;
import com.kuuhaku.robot.service.PetPetService;
import com.kuuhaku.robot.service.QuizService;
import com.kuuhaku.robot.service.RepeatService;
import com.kuuhaku.robot.utils.ThreadUtil;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatChoice;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 3:15
 * @Description 复读机
 */
@HandlerComponent
@Slf4j
public class RepeatHandler {
    private static final ReentrantLock chatLock = new ReentrantLock();
    private static final ConcurrentHashMap<String, ImmutablePair<ReentrantLock, List<Message>>> messagesMap = new ConcurrentHashMap<>();
    private static boolean sendGIF = true;
    @Value("${robot.openai.host}")
    public String openAIHost;
    @Value("${robot.openai.api.key}")
    public String openAIApiKey;
    @Autowired
    private RepeatService repeatService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private PetPetService petPetService;
    @Autowired
    private QuizService quizService;
    @Autowired
    private AudioService audioService;
    @Autowired
    private PetPetHandler petPetHandler;
    @Autowired
    private ProxyConfig proxyConfig;
    private OpenAiClient openAiClient;

    @PostConstruct
    public void init() {
        OpenAiClient.Builder builder = OpenAiClient.builder()
                .apiKey(openAIApiKey)
                .connectTimeout(50)
                .writeTimeout(50)
                .readTimeout(50)
                .apiHost(openAIHost);
        if (proxyConfig.getProtocol() != null) {
            builder.proxy(new Proxy(proxyConfig.getProtocol(), new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort())));
        }
        openAiClient = builder.build();
    }


    // 复读QQ群消息
    @Handler(order = -10)
    public void repeat(ChannelContext ctx) {
        repeatService.tryRepeat(ctx.event());
    }


    // 记录QQ群图片的地址
    @Handler(order = -9)
    public void printImageUrl(ChannelContext ctx) {
        FlashImage flashImage = ctx.event().getMessage().get(FlashImage.Key);
        if (flashImage != null) {
            String message = ctx.event().getMessage().contentToString();
            log.info("{接收到闪照} userId:{},userNick:{},msg:{}", ctx.senderId(), ctx.nickname(), message);
            ctx.group().sendMessage(flashImage);
            String s = Image.queryUrl(flashImage.getImage());
            log.info("[闪照] url = " + s);
            downloadService.download(s, downloadService.getRandomPath() + "_flash.png");
        }
        Image image = ctx.event().getMessage().get(Image.Key);
        if (image != null) {
            String s = Image.queryUrl(image);
            log.info("[图片] url={}, sender={}, nickname={}", s, ctx.senderId(), ctx.nickname());
        }
    }

    // 反转gif图片
    @Handler(order = -5)
    public void reverseGIF(ChannelContext ctx) {
        if (!sendGIF) {
            return;
        }
        petPetService.reverseGIF(ctx.event());
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"开启复读gif"}, types = {HandlerMatchType.COMPLETE}, description = "机器人会倒放50kb以下的gif")
    public void setSendGIFTrue(ChannelContext ctx) {
        sendGIF = true;
        ctx.group().sendMessage("已开启");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"关闭复读gif"}, types = {HandlerMatchType.COMPLETE}, description = "机器人不会倒放gif")
    public void setSendGIFFalse(ChannelContext ctx) {
        sendGIF = false;
        ctx.group().sendMessage("已关闭");
    }

    @Permission()
    @Handler(values = {"复读"}, types = {HandlerMatchType.CONTAINS}, description = "机器人帮发送图片，格式如[复读+要发的图片]，需要在一条消息内发送")
    public void forward(ChannelContext ctx) {
        MessageEvent event = ctx.event();
        User sender = event.getSender();
        Image image = ctx.event().getMessage().get(Image.Key);
        if (image != null) {
            ctx.group().sendMessage(image);
            Group group = ctx.event().getBot().getGroup(ctx.groupId());
            if (group != null) {
                if (group.getBotPermission().getLevel() == MemberPermission.ADMINISTRATOR.getLevel()) {
                    if (sender instanceof NormalMember normalMember) {
                        if (normalMember.getPermission().getLevel() == MemberPermission.MEMBER.getLevel()) {
                            Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
                        }
                    }
                }
            }
        }
    }

    // ChatGPT聊天功能，如果在消息最后加上doc则会回复文本否则转语音
    @Permission
    @Handler(values = {"@${robot.account}"}, types = {HandlerMatchType.START}, description = "人工智障在线聊天，格式如[@机器人 你好]")
    public void reply(ChannelContext ctx) {
        if (ctx.command().isEmpty()) {
            return;
        }

        // 去除@xxxxx，获取真实消息
        String pre = "@" + ctx.event().getBot().getId() + " ";
        String msg = ctx.command().getMsg().substring(pre.length());
        if (msg.equals("")) {
            return;
        }
        // 特殊处理@机器人发表情包某些场景
        if (msg.equals("搓")) {
            petPetHandler.toPetPet(ctx);
            return;
        }
        if (msg.equals("爬")) {
            petPetHandler.toPa(ctx);
            return;
        }
        if (msg.equals("裂开")) {
            petPetHandler.toRipped(ctx);
            return;
        }
        if (msg.equals("丢")) {
            petPetHandler.toDiu(ctx);
            return;
        }

        log.info("start invoke chat gpt api");
        boolean useDoc = false;
        msg = msg.trim();
        if (msg.equals("")) {
            return;
        }
        if (msg.endsWith("doc")) {
            msg = msg.substring(0, msg.length() - 3);
            useDoc = true;
        }
        chatLock.lock();
        ImmutablePair<ReentrantLock, List<Message>> pair = messagesMap.get(ctx.groupIdStr());
        if (pair == null) {
            pair = new ImmutablePair<>(new ReentrantLock(), new ArrayList<>());
            messagesMap.put(ctx.groupIdStr(), pair);
        }
        chatLock.unlock();
        ReentrantLock lock = pair.getLeft();
        List<Message> lastMessages = pair.getRight();
        for (int i = 0; i < 6; i++) {
            boolean locked = lock.tryLock();
            if (!locked) {
                ctx.group().sendMessage("正在进行中，请稍后");
                log.info("get chat gpt api lock failed");
                return;
            }
            try {
                if (lastMessages.isEmpty()) {
                    lastMessages.add(Message.builder().role(Message.Role.SYSTEM).content(msg).build());
                }
                lastMessages.add(Message.builder().role(Message.Role.USER).content(msg).build());
                ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(lastMessages);
                ChatChoice chatChoice = chatCompletionResponse.getChoices().get(0);
                if (chatChoice.getFinishReason().equals("stop") || chatChoice.getFinishReason().equals("content_filter")) {
                    Message reply = chatChoice.getMessage();
                    log.info("chatGPT reply:{{}}", reply);
                    lastMessages.add(reply);
                    if (useDoc) {
                        ctx.group().sendMessage(reply.getContent());
                    } else {
                        ctx.group().sendMessage(audioService.ReadText(ctx, reply.getContent(), true));
                    }
                } else if (chatChoice.getFinishReason().equals("length")) {
                    ctx.group().sendMessage("对话过长，清理本次上下文，请重新开始，当前对话token:" + chatCompletionResponse.getUsage().toString());
                    lastMessages.clear();
                } else {
                    ctx.group().sendMessage("异常返回,清理本次上下文");
                    lastMessages.clear();
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        ctx.group().sendMessage("网络问题请稍后再试");
    }

    @Permission
    @Handler(values = {"清理上下文"}, types = {HandlerMatchType.COMPLETE}, description = "清理chatGPT当前会话的上下文")
    public void clean(ChannelContext ctx) {
        chatLock.lock();
        ImmutablePair<ReentrantLock, List<Message>> pair = messagesMap.get(ctx.groupIdStr());
        if (pair == null) {
            ctx.group().sendMessage("当前上下文为空");
            return;
        }
        chatLock.unlock();
        pair.getLeft().lock();
        pair.getRight().clear();
        pair.getLeft().unlock();
        ctx.group().sendMessage("ok");
    }


    @Handler(values = {"出题"}, types = {HandlerMatchType.COMPLETE}, description = "bilibili入站题，没答案")
    public void quiz(ChannelContext ctx) {
        Quiz quiz = quizService.randomQuiz();
        ctx.group().sendMessage(quiz.getTitle());
        ThreadUtil.sleep(2000);
        MessageChain messages = MessageUtils.newChain();
        List<String> answers = quiz.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            messages = messages.plus("选项" + (i + 1) + ": " + answers.get(i)).plus("\n");
        }
        ctx.group().sendMessage(messages);
    }


    // 加速git图片
    @Permission(level = PermissionRank.MEMBER)
    @Handler(values = {"gif加速"}, types = {HandlerMatchType.CONTAINS}, description = "2倍加速gif")
    public void fastGIFFalse(ChannelContext ctx) {
        petPetService.fastGIF(ctx.event());
    }
}
