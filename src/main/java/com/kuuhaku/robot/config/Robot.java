package com.kuuhaku.robot.config;

import com.kuuhaku.robot.core.service.CommandService;
import com.kuuhaku.robot.event.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author by kuuhaku
 * @Date 2021/2/10 17:20
 * @Description QQ机器人
 */
@Getter
@Setter
@Slf4j
@Component
public class Robot {
    /**
     * 保存登录设备信息，第一次随机启动之后启动便使用该信息，否则每次都得验证
     */
    private static final String DEVICE_INFO = "deviceInfo.json";
    private final Bot bot;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
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
        //注册事件，需要可以自己添加别的类型
        var events = Arrays.asList(
                messageEvents, messageRecallEvents, nudgeEvents,
                memberJoinEvents, memberLeaveEvents, memberJoinRequestEvents
        );
        for (var event : events) {
            GlobalEventChannel.INSTANCE.registerListenerHost(event);
        }
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
