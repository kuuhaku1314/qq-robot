package com.kuuhaku.robot;

import com.kuuhaku.robot.config.Robot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/10 16:21
 * @Description boot
 */

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        // 设置为非webApp
        app.setWebApplicationType(WebApplicationType.NONE);
        ApplicationContext context = app.run(args);
        Robot robot = context.getBean(Robot.class);
        robot.start();
        Runtime.getRuntime().addShutdownHook(new Thread(robot::close));
    }

}
