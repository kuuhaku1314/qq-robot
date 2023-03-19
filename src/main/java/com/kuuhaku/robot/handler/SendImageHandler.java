package com.kuuhaku.robot.handler;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.ImageService;
import com.kuuhaku.robot.core.service.TaskService;
import com.kuuhaku.robot.entity.Illust;
import com.kuuhaku.robot.service.ImageApiService;
import com.kuuhaku.robot.service.KumoService;
import com.kuuhaku.robot.utils.*;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author by kuuhaku
 * @Date 2021/2/12 2:04
 * @Description 发送图片
 */
@HandlerComponent
@Slf4j
public class SendImageHandler {
    public static List<String> drawlotsPaths;
    public static Set<String> drawlotsSet = new HashSet<>();
    public static Set<String> tarotSet = new HashSet<>();
    @Value("${robot.drawlots.path}")
    public String drawlotsPath;
    @Value("${robot.tarot.path}")
    public String tarotPath;
    @Value("${robot.life_restart.cmd}")
    public String cmdLifeRestart;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private ImageApiService imageApiService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private KumoService kumoService;
    private final ConcurrentHashMap<Long, String> lockMap = new ConcurrentHashMap<>();
    private final Map<String, String> tarotMap = new HashMap<>();

    @PostConstruct
    void init() {
        drawlotsPaths = DownloadService.getImageFiles(drawlotsPath);
        tarotMap.put("圣杯1", "家庭生活之幸福，别的牌可给予其更多内涵，如宾客来访、宴席、吵架");
        tarotMap.put("圣杯10", "家庭幸福，预料之外的好消息");
        tarotMap.put("圣杯2", "成功和好运，但细心、专心会是获取它们的必要条件");
        tarotMap.put("圣杯3", "切忌轻率、鲁莽，它们会给事业带来厄运");
        tarotMap.put("圣杯4", "不易说服的人，未婚的男子或女子，婚姻推迟");
        tarotMap.put("圣杯5", "无根据的嫉妒，缺乏果断误了大事，且逃避责任");
        tarotMap.put("圣杯6", "轻信，你容易被欺骗，特别是被不值得信任的同伴欺骗");
        tarotMap.put("圣杯7", "善变或食言，提防过分乐观的朋友和无主见的熟人");
        tarotMap.put("圣杯8", "令人愉快的公司或友谊，聚合或有计划的庆祝活动");
        tarotMap.put("圣杯9", "梦里与愿望实现，好运与财富");
        tarotMap.put("圣杯侍者", "一个永远的亲密朋友，或许是分别很久的童年朋友或初恋情人");
        tarotMap.put("圣杯国王", "诚实、善良的男子，但容易草率地做出决定，并不可依赖");
        tarotMap.put("圣杯王后", "忠诚、钟情的女人，温柔大方，惹人怜爱");
        tarotMap.put("圣杯骑士", "假朋友，来自远方陌生的人，勾引者，应当把握当前命运");
        tarotMap.put("宝剑1", "不幸，坏消息，充满嫉妒的情感");
        tarotMap.put("宝剑10", "悲伤，否定好兆头");
        tarotMap.put("宝剑2", "变化，分离");
        tarotMap.put("宝剑3", "一次旅行，爱情或婚姻的不幸");
        tarotMap.put("宝剑4", "疾病，经济困难，嫉妒，各种小灾难拖延工作的进度");
        tarotMap.put("宝剑5", "克服困难，获得生意成功或者和谐的伙伴");
        tarotMap.put("宝剑6", "只要有坚韧不拔的毅力，就能完成计划");
        tarotMap.put("宝剑7", "与朋友争吵，招来许多麻烦");
        tarotMap.put("宝剑8", "谨慎，看似朋友的人可能成为敌人");
        tarotMap.put("宝剑9", "疾病、灾难、或各种不幸");
        tarotMap.put("宝剑侍者", "嫉妒或者懒惰的人，事业上的障碍，或许是骗子");
        tarotMap.put("宝剑国王", "野心勃勃、妄想驾驭一切");
        tarotMap.put("宝剑王后", "奸诈，不忠，一个寡妇或被抛弃的人");
        tarotMap.put("宝剑骑士", "传奇中的豪爽人物，喜好奢侈放纵，但勇敢、有创业精神");
        tarotMap.put("权杖1", "财富与事业的成功，终生的朋友和宁静的心境");
        tarotMap.put("权杖10", "意想不到的好运，长途旅行，但可能会失去一个亲密的朋友");
        tarotMap.put("权杖2", "失望，来自朋友或生意伙伴的反对");
        tarotMap.put("权杖3", "不止一次的婚姻");
        tarotMap.put("权杖4", "谨防一个项目的失败，虚假或不可靠的朋友起到了破坏作用");
        tarotMap.put("权杖5", "娶一个富婆");
        tarotMap.put("权杖6", "有利可图的合伙");
        tarotMap.put("权杖7", "好运与幸福，但应提防某个异性");
        tarotMap.put("权杖8", "贪婪，可能花掉不属于自己的钱");
        tarotMap.put("权杖9", "和朋友争辩，固执的争吵");
        tarotMap.put("权杖侍者", "一个诚挚但缺乏耐心的朋友，善意的奉承");
        tarotMap.put("权杖国王", "一个诚挚的男人，慷慨忠实");
        tarotMap.put("权杖王后", "一个亲切善良的人，但爱发脾气");
        tarotMap.put("权杖骑士", "幸运地得到亲人或陌生人的帮助");
        tarotMap.put("钱币1", "重要的消息，或珍贵的礼物");
        tarotMap.put("钱币10", "把钱作为目标，但并不一定会如愿以偿");
        tarotMap.put("钱币2", "热恋，但会遭到朋友反对");
        tarotMap.put("钱币3", "争吵，官司，或家庭纠纷");
        tarotMap.put("钱币4", "不幸或秘密的背叛，来自不忠的朋友，或家庭纠纷");
        tarotMap.put("钱币5", "意外的消息，生意成功、愿望实现、或美满的婚姻");
        tarotMap.put("钱币6", "早婚，但也会早早结束，第二次婚姻也无好兆头");
        tarotMap.put("钱币7", "谎言，谣言，恶意的批评，运气糟透的赌徒");
        tarotMap.put("钱币8", "晚年婚姻，或一次旅行，可能带来结合");
        tarotMap.put("钱币9", "强烈的旅行愿望，嗜好冒险，渴望生命得到改变");
        tarotMap.put("钱币侍者", "一个自私、嫉妒的亲戚，或一个带来坏消息的使者");
        tarotMap.put("钱币国王", "一个脾气粗暴的男人，固执而充满复仇心，与他对抗会招来危险");
        tarotMap.put("钱币王后", "卖弄风情的女人，乐于干涉别人的事情，诽谤和谣言");
        tarotMap.put("钱币骑士", "一个有耐心、有恒心的男人，发明家或科学家");
        tarotMap.put("愚者正位", "活在当下，或随遇而安，如果每天都很重试，便能回味无穷");
        tarotMap.put("愚者逆位", "时机的关键所在，表示还不是时机，也代表没能把握住时机，或太固执于过去的计划，过分依赖他人的建议");
        tarotMap.put("魔术师正位", "富有外交手腕，但需要坚定的意志和正当的目的才能把它发挥出来。成功可以获得巨大收获；但失败的话...");
        tarotMap.put("魔术师逆位", "毫无意义的投机心态。漫无目的、缺乏自律，也暗示精神上的困扰。极端下意味着丧失良知和反社会");
        tarotMap.put("女教皇正位", "宁静、直觉、含蓄、谨慎，被动接受以得到发展");
        tarotMap.put("女教皇逆位", "诡异、猜疑、冷漠、迟缓，内心发展后，回到了现实生活");
        tarotMap.put("女皇正位", "具有魅力、优雅和毫无保留的爱，有创造力和聪明才智");
        tarotMap.put("女皇逆位", "自负、矫情，无法容忍缺陷。不应过于理想化");
        tarotMap.put("皇帝正位", "坚强的意志和稳固的力量，通过努力和自律达到成功");
        tarotMap.put("皇帝逆位", "任性、暴虐和残忍，意味着由于缺乏自律而失败，高处不胜寒");
        tarotMap.put("教皇正位", "信心十足，不疑惑，对事情有正确的理解力，寻找新的方法，可能感到阻力，但事实会证明一切");
        tarotMap.put("教皇逆位", "爱说教，唱高调以及独断，也代表新的观念形成、或拒绝流俗。为自己的人生写剧本，按自己对生命的理解而活");
        tarotMap.put("恋人正位", "道德、美学、更深层次的精神和肉体上的渴望，暗示一段新的关系，或已有关系的新形态，也暗示沉醉于爱河");
        tarotMap.put("恋人逆位", "欲求不满、多愁善感、迟疑不决。意味着任何追求新阶段的努力都只建立于期待和梦想，也可以意味一段关系的结束");
        tarotMap.put("战车正位", "成功、有才能、有效率，抛开过去的束缚。如果牌阵总体结果不好，应该考虑有哪方面过于激烈");
        tarotMap.put("战车逆位", "暗示专治的态度和拙劣的方向感，可能被情绪懵逼了视线。太过多愁善感");
        tarotMap.put("力量正位", "代表个人魅力与追求成功的决心，象征爱与坚强的意志力，可驾驭不易控制的能量，拥有优势力量");
        tarotMap.put("力量逆位", "自满、滥权，对人生无望，害怕被热情和欲望冲昏头脑。去做自己想做的");
        tarotMap.put("隐士正位", "代表有所坚持，有目标、深沉且专注");
        tarotMap.put("隐士逆位", "代表不易原谅他人，可能感到寂寞。如果害怕放弃某些东西，会失去成长的机会");
        tarotMap.put("命运之轮正位", "代表无论喜欢与否，改变终将会到来");
        tarotMap.put("命运之轮逆位", "意味着要对抗改变可能很困难，但是成败输赢不是固定的，请迎接未来");
        tarotMap.put("正义正位", "正直、公平、诚实、纪律，只要对自己诚实，未来能得到改善；或代表成功解决某个争议");
        tarotMap.put("正义逆位", "暗示消极、不满的心态，代表无休止的争议或不协调。必须要找到原因，否则不和将一再出现");
        tarotMap.put("吊人正位", "代表有偿的牺牲，代表任性极限、解决问题、有人文特质，代表一段反省的时光、内在的平和宁静，也代表在不同角度，会有不同的感受");
        tarotMap.put("吊人逆位", "代表无偿的牺牲，代表精神有所局限且缺乏远见，代表听从他人期望而不顺从自己的想法。顺从自有好处");
        tarotMap.put("死神正位", "代表顺从变化，放下曾经紧抓不放的事物，会得到新生；同时意味接受死亡，便会活得更充实");
        tarotMap.put("死神逆位", "意味并不相信改变会带来好结果，因此拒绝改变；代表掩饰绝望");
        tarotMap.put("节制正位", "代表节制，调试热情，不致过分越轨，还代表运用知识和理解力来条件行为的能力");
        tarotMap.put("节制逆位", "代表轻浮和过度追求时髦，也可代表学习和旅行");
        tarotMap.put("恶魔正位", "代表感官的魅力和热情的表达，接受了糟糕的状态而不愿改变");
        tarotMap.put("恶魔逆位", "即使肢体上受到束缚，精神仍可以翱翔，积极寻找改变的方法，放下控制欲，才能发现真正的自由");
        tarotMap.put("塔正位", "表示能接受挫折，勇敢克服困难，或许感到深深的痛苦与失望，但都是为了自己能有所成长");
        tarotMap.put("塔逆位", "代表得意忘形、自作自受、沉溺于虚幻的想象。无法抗拒改变，其终究会发生");
        tarotMap.put("星星正位", "代表着乐观与无限的希望，对自己充满信心，将迎来一段心平气和的时光");
        tarotMap.put("星星逆位", "需要心灵上的自由。适当舍弃没有价值的事物，会让你看的更清楚");
        tarotMap.put("月亮正位", "意味着敏感、体谅、感同身受，代表对圆满的不安，越幸福的时候，越担心不幸会到来");
        tarotMap.put("月亮逆位", "代表感情上的顺从、被动和缺乏自我，面对心中的想法，才能解决问题");
        tarotMap.put("太阳正位", "代表着具有激情、人际和谐以及美好名声等正面的特质，意味着有创造性的事物，可以在生活中感到快乐、爱与价值");
        tarotMap.put("太阳逆位", "代表骄傲、自负、虚伪等反面特征，然而，生命与世界任然支持你，它也可能暗示竞争，也许是自己与自己的竞争");
        tarotMap.put("审判正位", "代表具有超越自我、发觉无限潜力的特质，了解生命的相连，才能了解自己的精神意志、知道如何表达他");
        tarotMap.put("审判逆位", "代表着缺少应对忧郁的能力，而越是逃避，空虚感更加加深；试图弥补，解决之道来自内心");
        tarotMap.put("世界正位", "意味着报酬优厚，收获巨大");
        tarotMap.put("世界逆位", "预示巨大的障碍、涣散的精神及自怜的性格，同时也代表旅行；有人以为是成功带来了快乐，实是快乐带来了成功");
    }


    // @Permission
    // @Handler(values = {"我的词云"}, types = {HandlerMatchType.COMPLETE}, description = "发送我的关键词列表图片")
    public void sendKumo(ChannelContext ctx) {
        long id = ctx.senderId();

        taskService.submitTask(() -> {
            String put = lockMap.put(id, "");
            if (put == null) {
                try {
                    String kumoPath = kumoService.getKumoPath(id + "");
                    if (kumoPath == null) {
                        ctx.group().sendMessage("暂时不存在");
                        return;
                    }
                    Image image = imageService.uploadImage(kumoPath, ctx.event());
                    MessageChain result = imageService.parseMsgChainByImg(image);
                    ctx.group().sendMessage(result);
                    downloadService.deleteFile(kumoPath);
                } finally {
                    lockMap.remove(id);
                }
            }
        });
    }

    @Permission
    @Handler(values = {"色图", "涩图"}, types = {HandlerMatchType.COMPLETE,
            HandlerMatchType.COMPLETE}, description = "不准发")
    public void sendBadImage(ChannelContext ctx) {
        MessageChain result = MessageUtils.newChain();
        At at = new At(ctx.senderId());
        result = result.plus("你可少看点吧\uD83D\uDE4F").plus(at);
        ctx.group().sendMessage(result);
    }


    @Permission
    @Handler(values = {"二维码"}, types = {HandlerMatchType.START}, description = "生成二维码，格式[二维码 www.baidu.com]")
    public void sendCQCode(ChannelContext ctx) {
        if (ctx.command().params().size() != 1) {
            return;
        }
        String avatarUrl = ctx.event().getSender().getAvatarUrl();
        taskService.submitTask(() -> {
            String url = ctx.command().params().get(0);
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            //头像logo
            String logPath = downloadService.getRandomPngPath();
            downloadService.download(avatarUrl, logPath);
            String QrCodePath = downloadService.getRandomPngPath();
            File logoFile = new File(logPath);
            File QrCodeFile = new File(QrCodePath);
            try {
                ImageUtil.drawLogoQRCode(logoFile, QrCodeFile, url);
                Image image = imageService.uploadImage(QrCodePath, ctx.event());
                MessageChain result = imageService.parseMsgChainByImg(image);
                ctx.group().sendMessage(result);
            } catch (Exception e) {
                MessageChain result = MessageUtils.newChain();
                At at = new At(ctx.senderId());
                result = result.plus("生成失败").plus(at);
                ctx.group().sendMessage(result);
            } finally {
                downloadService.deleteFile(logPath);
                downloadService.deleteFile(QrCodePath);
            }
        });
    }

    @Permission
    @Handler(values = {"抽签", "运势"}, types = {HandlerMatchType.COMPLETE, HandlerMatchType.COMPLETE}, description = "每日抽签")
    public void drawlots(ChannelContext ctx) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        String dayStr = "" + year + month + day + ":";
        if (drawlotsSet.contains(dayStr + ctx.senderId())) {
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus("你今天已经抽过签了").plus(at);
            ctx.group().sendMessage(result);
            return;
        }
        int i = new Random(System.currentTimeMillis()).ints(0, drawlotsPaths.size()).limit(1).findFirst().getAsInt();
        taskService.submitTask(() -> {
            Image image = imageService.uploadImage(drawlotsPaths.get(i), ctx.event());
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus(at);
            result = result.plus(imageService.parseMsgChainByImg(image));
            ctx.group().sendMessage(result);
            drawlotsSet.add(dayStr + ctx.senderId());
        });
    }

    // @Permission
    // @Handler(values = {"变"}, types = {HandlerMatchType.START}, description = "真人头像生成二次元头像，效果不好已经废弃")
    public void bian(ChannelContext ctx) {
        MessageEvent event = ctx.event();
        Image image = ctx.event().getMessage().get(Image.Key);
        if (image != null) {
            String src = downloadService.getRandomPngPath();
            downloadService.download(Image.queryUrl(image), src);
            String des = downloadService.getRandomPngPath();
            try {
                if (HttpUtil.SendImage(src, des)) {
                    ctx.group().sendMessage(imageService.uploadImage(des, event));
                } else {
                    ctx.group().sendMessage("出现错误");
                }
            } catch (IOException e) {
                e.printStackTrace();
                ctx.group().sendMessage("出现错误");
            }
            downloadService.deleteFile(src);
            downloadService.deleteFile(des);
        }
    }

    // @Permission
    // @Handler(values = {"上色"}, types = {HandlerMatchType.START}, description = "给图片上色, 格式如[上色 + 某张图片]，耗性能不一定开")
    public void shangse(ChannelContext ctx) {
        MessageEvent event = ctx.event();
        int times = 1;
        times = RandomUtil.random(32);
        String faceID = 65535 + times + "";
        Image image = ctx.event().getMessage().get(Image.Key);
        if (image != null) {
            log.info("开始上色 faceID=" + faceID);
            String src = downloadService.getRandomPngPath();
            downloadService.download(Image.queryUrl(image), src);
            try {
                String path = HttpUtil.getColorImage(src, faceID, null, null, null, null);
                if (!path.equals("")) {
                    String localPath = downloadService.getRandomPngPath();
                    downloadService.download(path, localPath);
                    ctx.group().sendMessage(imageService.uploadImage(localPath, event));
                    downloadService.deleteFile(localPath);
                } else {
                    ctx.group().sendMessage("出现错误，可能太大了");
                }
                log.info("上色完成");
            } catch (IOException e) {
                e.printStackTrace();
                ctx.group().sendMessage("出现错误");
            }
            downloadService.deleteFile(src);
        }
    }

    // @Permission
    // @Handler(values = {"打分"}, types = {HandlerMatchType.START}, description = "给图片打分, 格式如[打分 + 某张图片]，据说loli分比较高")
    public void reply(ChannelContext ctx) {
        Image image = ctx.event().getMessage().get(Image.Key);
        if (image != null) {
            try {
                if (Image.queryUrl(image).startsWith("http://c2cpicdw.qpic.cn")) {
                    ctx.group().sendMessage("获取不到图片");
                    return;
                }
                String reply = HttpUtil.get("http://localhost:8080/classify?url=" + HttpUtil.encodeURL(Image.queryUrl(image)));
                ctx.group().sendMessage(reply.substring(1, reply.length() - 3));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Permission
    @Handler(values = {"来张涩图"}, types = {HandlerMatchType.COMPLETE}, description = "网上随便找张图给你")
    public void randomImage(ChannelContext ctx) {
        taskService.submitTask(() -> {
            String imagePath = imageApiService.randomImagePath();
            if (imagePath == null) {
                ctx.group().sendMessage("获取不到涩图");
                return;
            }
            Image image = imageService.uploadImage(imagePath, ctx.event());
            MessageChain result = imageService.parseMsgChainByImg(image);
            ctx.group().sendMessage(result);
            downloadService.deleteFile(imagePath);
        });
    }

    //@Permission
    //@Handler(values = {"p图"}, types = {HandlerMatchType.COMPLETE}, description = "随机一张p站图")
    public void randomPixivImage(ChannelContext ctx) {
        String path = downloadService.getRandomPath();
        int id = RandomUtil.random(95931840) + 8;
        taskService.submitTask(() -> {
            try {
                String reply = null;
                reply = HttpUtil.get("http://localhost:8080/pixiv/download?path=" + path + "&id=" + id);
                log.info("随机涩图开始，id=" + id);
                if (reply.contains("错误")) {
                    reply = HttpUtil.get("http://localhost:8080/pixiv/download?path=" + path + "&id=" + (RandomUtil.random(95931840) + 8));
                }
                if (reply.contains("错误")) {
                    reply = HttpUtil.get("http://localhost:8080/pixiv/download?path=" + path + "&id=" + (RandomUtil.random(95931840) + 8));
                }
                if (reply.contains("错误")) {
                    reply = HttpUtil.get("http://localhost:8080/pixiv/download?path=" + path + "&id=" + (RandomUtil.random(95931840) + 8));
                }
                if (reply.contains("错误")) {
                    reply = HttpUtil.get("http://localhost:8080/pixiv/download?path=" + path + "&id=" + (RandomUtil.random(95931840) + 8));
                }
                if (reply.contains("错误")) {
                    log.info("reply=" + reply);
                    ctx.group().sendMessage("获取不到图片");
                    return;
                }
                Illust illust = JSONObject.parseObject(reply, Illust.class);
                String msg = illust.toString();
                ctx.group().sendMessage(msg);
                log.info(path);
                Image image = imageService.uploadImage(path, ctx.event());
                downloadService.deleteFile(path);
                List<String> previewList = new ArrayList<>();
                ForwardMessage.Node[] list = new ForwardMessage.Node[1];
                list[0] = new ForwardMessage.Node(1419989150, (int) (System.currentTimeMillis() / 1000), "いかり", image);
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
                ForwardMessage forwardMessage = new ForwardMessage(previewList, title, brief, source, summary, Arrays.asList(list));
                MessageReceipt<Contact> messageReceipt = ctx.group().sendMessage(forwardMessage);
                Runnable runnable = messageReceipt::recall;
                //timeTaskService.submitTask(runnable, 30, TimeUnit.SECONDS);
            } catch (IOException e) {
                ctx.group().sendMessage("出现错误");
                e.printStackTrace();
            }
        });
    }

    @Permission
    @Handler(values = {"人生重开"}, types = {HandlerMatchType.COMPLETE}, description = "人生重开小游戏")
    public void restartLife(ChannelContext ctx) {
        List<String> strings = CmdUtil.executeCmd(cmdLifeRestart);
        if (strings == null) {
            return;
        }
        List<List<String>> msgList = MojiUtil.split(strings, 27);
        List<String> pathList = Collections.synchronizedList(new ArrayList<>());
        ForwardMessage.Node[] list = new ForwardMessage.Node[msgList.size()];
        int timeMillis = (int) (System.currentTimeMillis() / 1000);
        CountDownLatch countDownLatch = new CountDownLatch(msgList.size());
        AtomicInteger i = new AtomicInteger(0);
        while (i.get() < msgList.size()) {
            int curIndex = i.getAndAdd(1);
            taskService.submitTask(() -> {
                String path = downloadService.getRandomPngPath();
                pathList.add(path);
                MojiUtil.createImage(msgList.get(curIndex).stream().reduce((a, b) -> a + "\n" + b).orElse(""), path, 1000, 1200);
                Image image = imageService.uploadImage(path, ctx.event());
                ForwardMessage.Node node = new ForwardMessage.Node(1419989150, timeMillis + curIndex * 10, "いかり", image);
                list[curIndex] = node;
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String path : pathList) {
            downloadService.deleteFile(path);
        }
        MessageChain result = MessageUtils.newChain();
        At at = new At(ctx.senderId());
        result = result.plus("下面是你这次的人生").plus(at);
        ctx.group().sendMessage(result);

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
        ForwardMessage forwardMessage = new ForwardMessage(previewList, title, brief, source, summary, Arrays.asList(list));
        ctx.group().sendMessage(forwardMessage);
        log.info("构造发送人生重开成功");
    }

    @Permission
    @Handler(values = {"塔罗牌占卜"}, types = {HandlerMatchType.COMPLETE}, description = "塔罗牌占卜")
    public void tarot(ChannelContext ctx) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        String dayStr = "" + year + month + day + ":";
        if (tarotSet.contains(dayStr + ctx.senderId())) {
            MessageChain result = MessageUtils.newChain();
            At at = new At(ctx.senderId());
            result = result.plus("你今天已经抽过塔罗牌或正在进行了").plus(at);
            ctx.group().sendMessage(result);
            return;
        }
        tarotSet.add(dayStr + ctx.senderId());

        MessageChain result = MessageUtils.newChain();
        At at = new At(ctx.senderId());
        result = result.plus(at).plus("\n").plus("第一张牌: 代表过去，即已经发生的事").plus("\n")
                .plus("第二张牌: 代表问题导致的局面").plus("\n")
                .plus("第三张牌: 表示困难可能有的解决方法");
        ctx.group().sendMessage(result);
        ThreadUtil.sleep(5000);
        ArrayList<String> list = new ArrayList<>(tarotMap.keySet());
        int i1 = RandomUtil.random(list.size());
        String tarot1 = list.remove(i1);
        String mine = tarotMap.get(tarot1);
        String name = "";
        if (!tarot1.contains("愚者")) {
            name = tarot1.replace("正位", "").replace("逆位", "");
        } else {
            name = tarot1;
        }
        ctx.group().sendMessage(MessageUtils.newChain().plus(at).plus("\n").plus("第一张牌: 代表过去，即已经发生的事，你抽中了[" + tarot1 + "]"));
        ThreadUtil.sleep(1000);
        Image image = imageService.uploadImage(tarotPath + name + ".jpg", ctx.event());
        ctx.group().sendMessage(image);
        ThreadUtil.sleep(1000);
        ctx.group().sendMessage("[" + tarot1 + "]" + mine);

        ThreadUtil.sleep(5000);
        int i2 = RandomUtil.random(list.size());
        String tarot2 = list.remove(i2);
        mine = tarotMap.get(tarot2);
        if (!tarot2.contains("愚者")) {
            name = tarot2.replace("正位", "").replace("逆位", "");
        } else {
            name = tarot2;
        }
        ctx.group().sendMessage(MessageUtils.newChain().plus(at).plus("\n").plus("第二张牌: 代表问题导致的局面，你抽中了[" + tarot2 + "]"));
        ThreadUtil.sleep(1000);
        image = imageService.uploadImage(tarotPath + name + ".jpg", ctx.event());
        ctx.group().sendMessage(image);
        ThreadUtil.sleep(1000);
        ctx.group().sendMessage("[" + tarot2 + "]" + mine);

        ThreadUtil.sleep(5000);
        int i3 = RandomUtil.random(list.size());
        String tarot3 = list.get(i3);
        mine = tarotMap.get(tarot3);
        if (!tarot3.contains("愚者")) {
            name = tarot3.replace("正位", "").replace("逆位", "");
        } else {
            name = tarot3;
        }
        ctx.group().sendMessage(MessageUtils.newChain().plus(at).plus("\n").plus("第三张牌: 表示困难可能有的解决方法，你抽中了[" + tarot3 + "]"));
        image = imageService.uploadImage(tarotPath + name + ".jpg", ctx.event());
        ThreadUtil.sleep(1000);
        ctx.group().sendMessage(image);
        ThreadUtil.sleep(1000);
        ctx.group().sendMessage("[" + tarot3 + "]" + mine);
    }
}
