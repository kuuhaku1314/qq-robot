package com.kuuhaku.robot.service;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.biliClient.BiliClient;
import com.kuuhaku.robot.biliClient.BiliClientFactor;
import com.kuuhaku.robot.biliClient.model.dynamic.*;
import com.kuuhaku.robot.config.Robot;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.ImageService;
import com.kuuhaku.robot.core.service.TaskService;
import com.kuuhaku.robot.entity.bilibili.DynamicPush;
import com.kuuhaku.robot.utils.HttpUtil;
import com.kuuhaku.robot.utils.ImageUtil;
import com.kuuhaku.robot.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * @author by kuuhaku
 * @date 2022/5/22 20:35
 * @description
 */
@Service
@Slf4j
public class BilibiliService {
    private static final Map<Long, Set<Long>> uidMap = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> pushGroups = new ConcurrentHashMap<>();
    private static final Map<Long, String> nameMap = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> newAddGroup = new ConcurrentHashMap<>();
    private static final Set<Long> pullUidSet = new CopyOnWriteArraySet<>();
    public final int corePoolSize = 2;
    public final int maximumPoolSize = 2;
    public final long keepAlive = 180;
    public final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(100);
    public final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("work-thread---" + count.getAndAdd(1));
            return new Thread(r);
        }
    };
    private final BlockingQueue<DynamicPush> messageQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAlive, TimeUnit.SECONDS, queue, threadFactory);
    private final DynamicFactory dynamicFactory = new DynamicFactory();
    private final String dumpPath = "src\\main\\resources\\dump\\dump_uid_gid.txt";
    private final String backDumpPath = "src\\main\\resources\\dump\\dump_uid_gid_back.txt";
    private final Pattern filter = Pattern.compile("\\[.*?_.*?]");
    @Autowired
    private ImageService imageService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private Robot robot;
    @Autowired
    private TaskService taskService;

    public static void main(String[] args) {
        System.out.println(new BilibiliService().filter.matcher("雫るる_Official转发动态啦!\n" +
                "坏了，分母多了\n" +
                "发布于:2022-06-10 20:12:26\n" +
                "\n" +
                "原动态如下:\n" +
                "@雫るる_Official 首先恭喜lu宝3周年快乐！[雫るる_爱][雫るる_爱][雫るる_爱] 虽然我才看你直播3天但是问题不大[雫るる_贴贴] 在你出道三周年之际我也给大伙来点福利！[雫るる_好耶][雫るる_好耶][雫るる_好耶]\n" +
                "弹幕们不是老说富哥V50么 现在你们机会来了抽取50位幸运儿送出价值55的LULU永久装扮给你们! 如果在已经有了的情况下会折现成50RMB！ [雫るる_馋馋][雫るる_馋馋][雫るる_馋馋]\n" +
                "重点来了！下面说下抽奖方式！！！关注@雫るる_Official拥有粉丝牌 转发并评论此动态便可参与本次抽奖！[雫るる_嚣张][雫るる_嚣张][雫るる_嚣张]\n" +
                "截止日期2022.6.16日0时 开奖日期2020.6.16日 请在开奖期间打开关注列表，否则取消资格。 抽奖结果我自己审核，重度抽奖号将被取消资格。谢谢各位对LULU的支持！[雫るる_喜欢][雫るる_喜欢][雫るる_喜欢][雫るる_喜欢][雫るる_喜欢][雫るる_喜欢]\n" +
                "当前回复人数:107").replaceAll(""));
    }

    @PostConstruct
    void init() {
        recover();
    }

    public void start() {
        asyncPullNewestDynamic();
        asyncPushNewestDynamic();
    }

    public String addPushGroup(Long groupID, Long uid, boolean isNew) {
        String name = nameMap.containsKey(uid) ? nameMap.get(uid) : getName(uid);
        if (name == null || "".equals(name)) {
            return null;
        }
        Set<Long> set = pushGroups.get(uid);
        if (set == null) {
            pushGroups.put(uid, new HashSet<Long>() {{
                add(groupID);
            }});
            nameMap.put(uid, name);
        } else {
            set.add(groupID);
        }
        // 是新订阅
        if (isNew) {
            Set<Long> groups = newAddGroup.get(uid);
            if (groups == null) {
                newAddGroup.put(uid, new HashSet<Long>() {{
                    add(groupID);
                }});
            } else {
                groups.add(groupID);
            }
        }
        if (!uidMap.containsKey(uid)) {
            log.info("添加订阅uid=" + uid + ",名字=" + name);
            uidMap.put(uid, new HashSet<>());
        }
        return name;
    }

    public MessageChain toContent(Dynamic dynamic) {
        return dynamicFactory.getInstance(dynamic).toMsg();
    }

    public void removePushGroup(Long groupID, Long uid) {
        Set<Long> set = pushGroups.get(uid);
        if (set != null) {
            set.remove(groupID);
            if (set.isEmpty()) {
                uidMap.remove(uid);
            }
        }
        Set<Long> longSet = newAddGroup.get(uid);
        if (longSet != null) {
            longSet.remove(groupID);
        }
    }

    public List<String> subscriptionList(Long groupID) {
        List<String> subscriptionList = new ArrayList<>();
        pushGroups.forEach((k, v) -> {
            if (v.contains(groupID)) {
                subscriptionList.add("uid [" + k + "]\n" + "昵称 [" + nameMap.get(k) + "]");
            }
        });
        return subscriptionList;
    }

    private String getName(Long uid) {
        try {
            String json = HttpUtil.get("https://api.bilibili.com/x/space/acc/info?mid=" + uid);
            JSONObject data = JSONObject.parseObject(json).getJSONObject("data");
            if (data == null) {
                return null;
            }
            return data.getString("name");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void dump(String path) {
        List<String> lines = new ArrayList<>();
        pushGroups.forEach((k, v) -> {
            for (Long gid : v) {
                lines.add(k + " " + gid);
            }
        });
        try {
            Path filePath = Paths.get(path);
            File file = filePath.toFile();
            if (file.exists()) {
                if (file.isDirectory()) {
                    throw new RuntimeException("dump file path is directory path");
                }
                Files.delete(Paths.get(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
            lines.forEach(line -> {
                try {
                    writer.write(line);
                    writer.write('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.flush();
            writer.close();
            log.info("dump ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dump() {
        dump(dumpPath);
    }

    public void backDump() {
        dump(backDumpPath);
    }

    private void recover() {
        try {
            String rawStr = new String(Files.readAllBytes(Paths.get(dumpPath)));
            String[] lines = rawStr.split("\n");
            List<String> list = new ArrayList<>(Arrays.asList(lines));
            log.info("当前订阅条目数量=" + lines.length);
            list.forEach(e -> {
                log.info("load条目:" + e);
                if (e.length() != 0) {
                    String[] pair = e.split(" ");
                    if (pair.length != 2) {
                        throw new RuntimeException("wrong data");
                    }
                    if (!NumberUtils.isNumber(pair[0]) || !NumberUtils.isNumber(pair[1])) {
                        throw new RuntimeException("wrong uid or gid");
                    }
                    Long uid = NumberUtils.toLong(pair[0]);
                    Long gid = NumberUtils.toLong(pair[1]);
                    addPushGroup(gid, uid, false);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void asyncPullNewestDynamic() {
        new Thread(() -> {
            log.info("async pull dynamic from bilibili start");
            BiliClient client = BiliClientFactor.getClient();
            while (true) {
                HashMap<Long, Set<Long>> snapshot = new HashMap<>(uidMap);
                try {
                    // 闭锁控制爬取频率
                    CountDownLatch latch = new CountDownLatch(snapshot.size());
                    AtomicInteger failCount = new AtomicInteger(snapshot.size());
                    snapshot.forEach((k, v) -> {
                        // 线程池控制爬取并发度
                        executor.submit(() -> {
                            try {
                                DynamicRoot list = client.dynamic().withHostUid(k).list();
                                failCount.addAndGet(-1);
                                Set<Long> dynamicIds = uidMap.get(k);
                                // 注意map可能改变了
                                if (dynamicIds == null || list.getItems() == null) {
                                    return;
                                }
                                // 是不是第一次拉取
                                boolean isFirst = !pullUidSet.contains(k);
                                Set<Long> groups = newAddGroup.get(k);
                                boolean hasNewPushGroup = groups != null && groups.size() != 0;
                                for (Dynamic item : list.getItems()) {
                                    // 需要发送的群
                                    boolean hasDynamic = dynamicIds.contains(item.getBase().getDynamic_id());
                                    if (isFirst || hasDynamic) {
                                        // 即使包含了，有新的push group还是算新消息，但只push一条
                                        if (hasNewPushGroup) {
                                            log.info("获取到新动态=" + item);
                                            // 发送给新加入的group
                                            messageQueue.add(new DynamicPush(k, item, new HashSet<>(groups), 0));
                                            hasNewPushGroup = false;
                                            // 清空
                                            groups.clear();
                                        }
                                        // isFirst进入此分支, 需要此操作
                                        if (isFirst) {
                                            dynamicIds.add(item.getBase().getDynamic_id());
                                        }
                                        continue;
                                    }
                                    dynamicIds.add(item.getBase().getDynamic_id());
                                    // 非第一次拉取有新动态则推送当前所有组
                                    log.info("获取到新动态=" + item);
                                    messageQueue.add(new DynamicPush(k, item, new HashSet<>(pushGroups.get(k)), 0));
                                }
                                if (isFirst) {
                                    // 确认爬取过
                                    pullUidSet.add(k);
                                }
                            } finally {
                                latch.countDown();
                            }
                        });
                    });
                    try {
                        latch.await();
                        // 有爬取失败的，基本是限制频率了，所以多休息会
                        if (failCount.get() > 0) {
                            log.info("频率被限制，当前总爬取[{}], 失败数量[{}]", snapshot.size(), failCount.get());
                            ThreadUtil.sleep(1000 * 60 * 10);
                            continue;
                        }
                        Calendar instance = Calendar.getInstance();
                        int hours = instance.get(Calendar.HOUR_OF_DAY);
                        // 这个时间段，限频高
                        if (hours >= 22 || hours <= 8) {
                            ThreadUtil.sleep(300000);
                        } else {
                            // 每轮后暂停60秒防止拦截
                            ThreadUtil.sleep(60000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public DynamicPush takeDynamicPush() throws InterruptedException {
        return messageQueue.take();
    }

    private void asyncPushNewestDynamic() {
        new Thread(() -> {
            log.info("async push dynamic to group task start");
            Bot bot = robot.getBot();
            while (true) {
                if (!bot.isOnline()) {
                    // 掉线了休息3秒
                    ThreadUtil.sleep(3000);
                    continue;
                }
                DynamicPush d = null;
                try {
                    DynamicPush dynamic = takeDynamicPush();
                    d = dynamic;
                    // 需要刷新,因为有可能中途进了新群
                    ContactList<Group> groups = bot.getGroups();
                    taskService.submitTask(() -> {
                        try {
                            Set<Long> pushGroups = dynamic.getGroups();
                            MessageChain content = toContent(dynamic.getDynamic());
                            for (Group group : groups) {
                                if (pushGroups.contains(group.getId())) {
                                    group.sendMessage(content);
                                    // 发完一个移除一个，避免出错重试后发送重复消息
                                    pushGroups.remove(group.getId());
                                }
                            }
                        } catch (Throwable e) {
                            if (dynamic.getRetryCount() > 5) {
                                log.error("推送消息失败超过5次，放弃推送此消息[{}]", dynamic);
                                return;
                            }
                            dynamic.setRetryCount(dynamic.getRetryCount() + 1);
                            final DynamicPush finalD = dynamic;
                            new Thread(() -> {
                                ThreadUtil.sleep(finalD.getRetryCount() * 30000L);
                                log.info("重新推送消息[{}]，当前重试次数[{}]", finalD, finalD.getRetryCount());
                                messageQueue.add(finalD);
                            }).start();
                            e.printStackTrace();
                        }
                    });
                } catch (Throwable e) {
                    // 发送过程中出错，放入消息队列重试
                    if (d != null) {
                        if (d.getRetryCount() > 5) {
                            log.error("推送消息失败超过5次，放弃推送此消息[{}]", d);
                            continue;
                        }
                        d.setRetryCount(d.getRetryCount() + 1);
                        final DynamicPush finalD = d;
                        new Thread(() -> {
                            ThreadUtil.sleep(finalD.getRetryCount() * 30000L);
                            log.info("重新推送消息[{}]，当前重试次数[{}]", finalD, finalD.getRetryCount());
                            messageQueue.add(finalD);
                        }).start();
                    }
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 定时任务用的，已废弃
    @Deprecated
    public List<DynamicPush> newestDynamic() {
        if (!robot.getBot().isOnline()) {
            log.info("当前robot掉线，放弃本次爬取");
            return new ArrayList<>();
        }
        List<DynamicPush> result = new ArrayList<>();
        BiliClient client = BiliClientFactor.getClient();
        HashMap<Long, Set<Long>> snapshot = new HashMap<>(uidMap);
        CountDownLatch latch = new CountDownLatch(snapshot.size());
        snapshot.forEach((k, v) -> {
            executor.submit(() -> {
                try {
                    DynamicRoot list = client.dynamic().withHostUid(k).list();
                    Set<Long> dynamicIds = uidMap.get(k);
                    // 注意map可能改变了
                    if (dynamicIds == null || list.getItems() == null) {
                        return;
                    }
                    // 是不是第一次拉取
                    boolean isFirst = !pullUidSet.contains(k);
                    Set<Long> groups = newAddGroup.get(k);
                    boolean hasNewPushGroup = groups != null && groups.size() != 0;
                    for (Dynamic item : list.getItems()) {
                        // 需要发送的群
                        boolean hasDynamic = dynamicIds.contains(item.getBase().getDynamic_id());
                        if (isFirst || hasDynamic) {
                            // 即使包含了，有新的push group还是算新消息，但只push一条
                            if (hasNewPushGroup) {
                                log.info("获取到新动态=" + item);
                                // 发送给新加入的group
                                result.add(new DynamicPush(k, item, new HashSet<>(groups), 0));
                                hasNewPushGroup = false;
                                // 清空
                                groups.clear();
                            }
                            // isFirst进入此分支, 需要此操作
                            if (isFirst) {
                                dynamicIds.add(item.getBase().getDynamic_id());
                            }
                            continue;
                        }
                        dynamicIds.add(item.getBase().getDynamic_id());
                        // 非第一次拉取有新动态则推送当前所有组
                        log.info("获取到新动态=" + item);
                        result.add(new DynamicPush(k, item, new HashSet<>(pushGroups.get(k)), 0));
                    }
                    if (isFirst) {
                        pullUidSet.add(k);
                    }
                } finally {
                    latch.countDown();
                }
            });
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    private MessageChain attachDynamicDisplay(DynamicDisplay display, MessageChain chain) {
        if (display == null || display.getAdd_on_card_info() == null || display.getAdd_on_card_info().size() == 0) {
            return chain;
        }
        AddOnCardInfo cardInfo = display.getAdd_on_card_info().get(0);
        ReserveAttachCard attachCard = cardInfo.getReserve_attach_card();
        if (attachCard == null) {
            return chain;
        }
        chain = chain.plus("\n\n附带一张小卡片\n");
        chain = chain.plus(attachCard.getTitle() + "\n");
        if (attachCard.getDesc_first() != null) {
            chain = chain.plus(attachCard.getDesc_first().getText() + "\n");
        }
        chain = chain.plus(attachCard.getDesc_second() + "\n" + "直播开始时间:");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        chain = chain.plus(sdf.format(new Date(attachCard.getLivePlanStartTime() * 1000L)));
        return chain;
    }

    abstract public class DynamicInstance {
        protected final Dynamic dynamic;

        DynamicInstance(Dynamic dynamic) {
            this.dynamic = dynamic;
        }

        public abstract MessageChain toMsg();
    }

    class DynamicFactory {
        DynamicInstance getInstance(Dynamic dynamic) {
            return switch (dynamic.getType()) {
                case WORD -> new WordDynamic(dynamic);
                case IMAGE -> new ImageDynamic(dynamic);
                case NOT_SUPPORTED -> new NotSupportedDynamic(dynamic);
                case VIDEO -> new VideoDynamic(dynamic);
                case REPOST -> new RepostDynamic(dynamic);
                case LIVE -> new liveDynamic(dynamic);
                case ACTIVITY -> new ActivityDynamic(dynamic);
                default -> throw new RuntimeException("unexpected dynamic type" + dynamic.getType());
            };
        }
    }

    class ImageDynamic extends DynamicInstance {

        ImageDynamic(Dynamic dynamic) {
            super(dynamic);
        }

        @Override
        public MessageChain toMsg() {
            MessageChain chain = MessageUtils.newChain();
            DynamicBase base = dynamic.getBase();
            // 是转发消息里消息的情况下base为null
            if (base != null) {
                chain = chain.plus(nameMap.get(base.getUid()) + "发布新动态啦!\n");
            }
            DynamicImage image = dynamic.getImage();
            if (image.getTitle() != null) {
                chain = chain.plus(image.getTitle() + "\n");
            }
            if (image.getDescription() != null) {
                chain = chain.plus(filter.matcher(image.getDescription()).replaceAll("") + "\n");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (base != null) {
                String timeStr = sdf.format(base.createTime());
                chain = chain.plus("发布于:" + timeStr + "\n");
            }
            List<Picture> pictures = image.getPictures();
            if (pictures == null) {
                return chain;
            }
            for (Picture picture : pictures) {
                String path = downloadService.getRandomPngPath();
                downloadService.download(picture.getImg_src(), path);
                chain = chain.plus(imageService.uploadImage(path, Objects.requireNonNull(robot.getBot().getGroup(1023903741L))));
                downloadService.deleteFile(path);
            }
            return attachDynamicDisplay(dynamic.getDisplay(), chain);
        }
    }

    class WordDynamic extends DynamicInstance {

        WordDynamic(Dynamic dynamic) {
            super(dynamic);
        }

        @Override
        public MessageChain toMsg() {
            MessageChain chain = MessageUtils.newChain();
            DynamicBase base = dynamic.getBase();
            // 是转发消息里消息的情况下base为null
            if (base != null) {
                chain = chain.plus(nameMap.get(base.getUid()) + "发布新动态啦!\n");
            }
            DynamicWord word = dynamic.getWord();
            chain = chain.plus(filter.matcher(word.getContent()).replaceAll("") + "\n");
            if (base != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(base.createTime());
                chain = chain.plus("发布于:" + timeStr + "\n");
            }
            chain = chain.plus("当前回复人数:" + word.getReply());
            return attachDynamicDisplay(dynamic.getDisplay(), chain);
        }
    }

    class VideoDynamic extends DynamicInstance {

        VideoDynamic(Dynamic dynamic) {
            super(dynamic);
        }

        @Override
        public MessageChain toMsg() {
            MessageChain chain = MessageUtils.newChain();
            DynamicBase base = dynamic.getBase();
            // 是转发消息里消息的情况下base为null
            if (base != null) {
                chain = chain.plus(nameMap.get(base.getUid()) + "发布视频新动态啦!\n");
            }
            DynamicVideo video = dynamic.getVideo();
            chain = chain.plus(filter.matcher(video.getDynamic()).replaceAll("") + "\n");
            if (base != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(base.createTime());
                chain = chain.plus("发布于:" + timeStr);
            }
            chain = chain.plus("\n\n");
            chain = chain.plus("标题:" + video.getTitle() + "\n");
            chain = chain.plus("简介:" + video.getDesc() + "\n");
            String path = downloadService.getRandomPngPath();
            downloadService.download(dynamic.getVideo().getPic(), path);
            Image image = imageService.uploadImage(path, Objects.requireNonNull(robot.getBot().getGroup(1023903741L)));
            chain = chain.plus(image);
            downloadService.deleteFile(path);
            chain = chain.plus("可以点击以下链接直接跳转哦!\n");
            chain = chain.plus(video.getShort_link());
            //目前短链看起来都一样
            //chain = chain.plus(video.getShort_link_v2());
            return chain;
        }
    }

    class RepostDynamic extends DynamicInstance {

        RepostDynamic(Dynamic dynamic) {
            super(dynamic);
        }

        @Override
        public MessageChain toMsg() {
            MessageChain chain = MessageUtils.newChain();
            chain = chain.plus(nameMap.get(dynamic.getBase().getUid()) + "转发动态啦!\n");
            DynamicRepost repost = dynamic.getRepost();
            chain = chain.plus(filter.matcher(repost.getContent()).replaceAll("") + "\n");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(dynamic.getBase().createTime());
            chain = chain.plus("发布于:" + timeStr);
            chain = chain.plus("\n\n原动态如下:\n");
            Dynamic dynamic = repost.getDynamic();
            DynamicInstance dynamicInstance = dynamicFactory.getInstance(dynamic);
            // 转发的转发，特殊处理，避免递归
            if (dynamicInstance instanceof RepostDynamic) {
                repost = dynamicInstance.dynamic.getRepost();
                if (repost != null) {
                    chain = chain.plus(repost.getContent() + "");
                }
            } else {
                chain = chain.plus(dynamicInstance.toMsg());
            }
            // card
            chain = attachDynamicDisplay(dynamic.getDisplay(), chain);
            // 转发下origin里有card(?)
            if (dynamic.getDisplay() != null) {
                chain = attachDynamicDisplay(dynamic.getDisplay().getOrigin(), chain);
            }
            return chain;
        }
    }

    class liveDynamic extends DynamicInstance {

        liveDynamic(Dynamic dynamic) {
            super(dynamic);
        }

        @Override
        public MessageChain toMsg() {
            MessageChain chain = MessageUtils.newChain();
            DynamicBase base = dynamic.getBase();
            if (base != null) {
                chain = chain.plus(nameMap.get(base.getUid()) + "发布直播动态啦!\n");
            }
            DynamicLive live = dynamic.getLive();
            if (live.getUname() != null) {
                chain = chain.plus("主播:" + live.getUname() + "\n");
            }
            chain = chain.plus("标题:" + live.getTitle() + "\n");
            if (live.getLive_time() != null) {
                chain = chain.plus("live time:" + live.getLive_time() + "\n");
            }
            if (live.getLive_start_time() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(live.getLive_start_time() * 1000);
                chain = chain.plus("\n直播开始于:" + timeStr + "\n");
            }
            chain = chain.plus(live.getWatched_show() + "\n");
            String path = downloadService.getRandomPngPath();
            downloadService.download(live.getCover(), path);
            Image image = imageService.uploadImage(path, Objects.requireNonNull(robot.getBot().getGroup(1023903741L)));
            chain = chain.plus(image);
            downloadService.deleteFile(path);

            if (base != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(base.createTime());
                chain = chain.plus("\n发布于:" + timeStr);
            }

            // 生成二维码
            String url = live.getLink();
            String avatarUrl = robot.getBot().getAvatarUrl();
            String logPath = downloadService.getRandomPngPath();
            downloadService.download(avatarUrl, logPath);
            String QrCodePath = downloadService.getRandomPngPath();
            File logoFile = new File(logPath);
            File QrCodeFile = new File(QrCodePath);
            try {
                ImageUtil.drawLogoQRCode(logoFile, QrCodeFile, url);
                image = imageService.uploadImage(QrCodePath, Objects.requireNonNull(robot.getBot().getGroup(1023903741L)));
            } catch (Exception e) {
                e.printStackTrace();
                return chain;
            } finally {
                downloadService.deleteFile(logPath);
                downloadService.deleteFile(QrCodePath);
            }

            chain = chain.plus("\n\n\n扫描下面二维码可直接观看哦!\n");
            chain = chain.plus(image);
            return attachDynamicDisplay(dynamic.getDisplay(), chain);
        }
    }

    class ActivityDynamic extends DynamicInstance {

        ActivityDynamic(Dynamic dynamic) {
            super(dynamic);
        }

        @Override
        public MessageChain toMsg() {
            MessageChain chain = MessageUtils.newChain();
            DynamicBase base = dynamic.getBase();
            // 是转发消息里消息的情况下base为null
            if (base != null) {
                chain = chain.plus(nameMap.get(base.getUid()) + "发布新动态啦!\n");
            }
            DynamicActivity activity = dynamic.getActivity();
            if (activity.getVest() != null) {
                chain = chain.plus(activity.getVest().getContent() + "\n");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (base != null) {
                String timeStr = sdf.format(base.createTime());
                chain = chain.plus("发布于:" + timeStr + "\n");
            }
            chain = chain.plus("\n活动如下:\n");
            if (activity.getSketch() != null) {
                chain = chain.plus(activity.getSketch().getTitle() + "\n");
                chain = chain.plus(activity.getSketch().getDesc_text() + "\n");
                String path = downloadService.getRandomPngPath();
                downloadService.download(activity.getSketch().getCover_url(), path);
                chain = chain.plus(imageService.uploadImage(path, Objects.requireNonNull(robot.getBot().getGroup(1023903741L))));
                downloadService.deleteFile(path);
                // 生成二维码
                String url = activity.getSketch().getTarget_url();
                String avatarUrl = robot.getBot().getAvatarUrl();
                String logPath = downloadService.getRandomPngPath();
                downloadService.download(avatarUrl, logPath);
                String QrCodePath = downloadService.getRandomPngPath();
                File logoFile = new File(logPath);
                File QrCodeFile = new File(QrCodePath);
                try {
                    ImageUtil.drawLogoQRCode(logoFile, QrCodeFile, url);
                    Image image = imageService.uploadImage(QrCodePath, Objects.requireNonNull(robot.getBot().getGroup(1023903741L)));
                    chain = chain.plus("\n\n\n扫描下面二维码可直接观看哦!\n");
                    chain = chain.plus(image);
                } catch (Exception e) {
                    e.printStackTrace();
                    return chain;
                } finally {
                    downloadService.deleteFile(logPath);
                    downloadService.deleteFile(QrCodePath);
                }
            }
            return attachDynamicDisplay(dynamic.getDisplay(), chain);
        }
    }

    class NotSupportedDynamic extends DynamicInstance {

        NotSupportedDynamic(Dynamic dynamic) {
            super(dynamic);
        }

        @Override
        public MessageChain toMsg() {
            MessageChain chain = MessageUtils.newChain();
            DynamicBase base = dynamic.getBase();
            if (base != null) {
                chain = chain.plus(nameMap.get(base.getUid()) + "发布动态啦!\n");
            }
            chain = chain.plus(dynamic.getNotSupported().getDesc() + "\n");
            if (base != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(base.createTime());
                chain = chain.plus("发布于:" + timeStr);
            }
            return chain;
        }
    }
}
