package com.kuuhaku.robot;

import com.kuuhaku.robot.config.ProxyConfig;
import com.kuuhaku.robot.config.Robot;
import com.kuuhaku.robot.service.BilibiliService;
import com.kuuhaku.robot.utils.FixProtocolVersion;
import com.kuuhaku.robot.utils.UnirestInstanceUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author by kuuhaku
 * @Date 2021/2/10 16:21
 * @Description boot
 */

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        init();
        var app = new SpringApplication(Application.class);
        // 设置为非webApp
        app.setWebApplicationType(WebApplicationType.NONE);
        var context = app.run(args);

        // 利用反射修改灯神sdk包的client设置代理，可以直接的不需要下面这两句
        ProxyConfig proxyConfig = context.getBean(ProxyConfig.class);
        UnirestInstanceUtil.HookUnirestInstance(proxyConfig.getHost(), proxyConfig.getPort());

        var robot = context.getBean(Robot.class);
        var bilibiliService = context.getBean(BilibiliService.class);
        robot.start();
        bilibiliService.start();
        Runtime.getRuntime().addShutdownHook(new Thread(robot::close));
        Runtime.getRuntime().addShutdownHook(new Thread(bilibiliService::dump));
    }

    static void init() {
        // System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
        // 暂时修复mirai登录版本问题
        FixProtocolVersion.update();
        // System.out.println(FixProtocolVersion.info());
        // System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
    }
}
