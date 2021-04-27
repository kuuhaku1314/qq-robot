package com.kuuhaku.robot.config;

import com.kuuhaku.robot.event.*;
import com.kuuhaku.robot.core.service.CommandService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.ListenerHost;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/10 17:20
 * @Description QQ机器人
 */
@Getter
@Setter
@Slf4j
@Component
public class Robot {
    private final Bot bot;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Value("${robot.account}")
    private Long account;
    @Value("${robot.password}")
    private String password;

    @Autowired
    private MessageEvents messageEvents;
    @Autowired
    private MessageRecallEvents messageRecallEvents;
    @Autowired
    private NudgeEvents nudgeEvents;
    @Autowired
    private MemberJoinEvents memberJoinEvents;
    @Autowired
    private MemberJoinRequestEvents memberJoinRequestEvents;
    @Autowired
    private MemberLeaveEvents memberLeaveEvents;
    @Autowired
    private CommandService commandService;
    @Autowired
    private ApplicationContext context;
    /**
     * 保存登录设备信息，第一次随机启动之后启动便使用该信息，否则每次都得验证
     */
    private static final String DEVICE_INFO = "deviceInfo.json";

    Robot(@Value("${robot.account}") long account, @Value("${robot.password}") String password) {
        this.bot = BotFactory.INSTANCE.newBot(account, password, new BotConfiguration() {{
            fileBasedDeviceInfo(DEVICE_INFO);
        }});
    }

    public void start() {
        init();
        bot.login();
        log.info("正在启动中，当前robot=[{}]，nickname=[{}]", bot.getId(), bot.getNick());
        log.info("登录成功");
        //注册事件，当前只注册了消息监听器，需要可以自己添加别的类型，比如戳一戳之类的
        List<ListenerHost> events = Arrays.asList(
                messageEvents, messageRecallEvents, nudgeEvents,
                memberJoinEvents, memberLeaveEvents, memberJoinRequestEvents
        );
        for (ListenerHost event : events) {
            GlobalEventChannel.INSTANCE.registerListenerHost(event);
        }

        //设置https协议，已解决SSL peer shut down incorrectly的异常
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
        executor.submit((Runnable) bot::join);
    }

    private void init() {
        log.info("正在初始化，注册指令中");
        commandService.registerAllCommand(context);
        log.info("初始化完成");
    }

    public void close() {
        bot.close();
        log.info("安全退出成功");
    }

}
