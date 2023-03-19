package com.kuuhaku.robot.core.chain;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author by kuuhaku
 * @Date 2021/4/24 9:26
 * @Description
 */
@Slf4j
public class ChannelPipeline {
    private static ChannelPipeline pipeline;
    private final List<Channel> channelGroup = new ArrayList<>();
    private final List<Channel> allExecuteChannel = new ArrayList<>();
    private final List<Channel> excludeChannel = new ArrayList<>();

    private ChannelPipeline(ApplicationContext context) {
        log.info("流水线初始化中");
        init(context);
        log.info("流水线初始化完成");
    }

    public static synchronized ChannelPipeline instance(ApplicationContext context) {
        if (pipeline == null) {
            pipeline = new ChannelPipeline(context);
        }
        return pipeline;
    }

    private void init(ApplicationContext context) {
        // 获取所有标记为service的class
        String[] handlerBeanNames = context.getBeanNamesForAnnotation(HandlerComponent.class);
        log.info("----->加载组件数量:{}", handlerBeanNames.length);
        for (String beanName : handlerBeanNames) {
            // 从上下文中取出该bean
            Object beanObj = context.getBean(beanName);
            // 查找处理请求的方法
            Method[] methods = beanObj.getClass().getMethods();
            for (Method method : methods) {
                // 查询标记有Handler注解的方法，并取出其值
                // 使用aop代理后无法使用method.getAnnotation()获取注解，改用工具类获取
                Handler handler = AnnotationUtils.findAnnotation(method, Handler.class);
                Environment environment = context.getEnvironment();
                if (handler != null) {
                    if (method.getParameterCount() != 1) {
                        log.error("参数格式不对，应为1个");
                        throw new RuntimeException();
                    }
                    if (method.getParameterTypes()[0] != ChannelContext.class) {
                        log.error("参数类型不对，应为ChannelContext.class");
                        throw new RuntimeException();
                    }
                    String[] values = handler.values();
                    for (int i = 0; i < values.length; i++) {
                        String v = environment.resolvePlaceholders(values[i]);
                        if (!v.equals(values[i])) {
                            log.info("replace annotation value, old=[{}], new=[{}]", values[i], v);
                            values[i] = v;
                        }
                    }
                    int[] types = handler.types();
                    if (values.length != types.length) {
                        log.error("注册指令时出错，请检查指令值与类型数量是否匹配");
                        log.error("values=[{}]", Arrays.toString(values));
                        throw new RuntimeException();
                    }
                    for (int i = 0; i < values.length; i++) {
                        ServiceMethod serviceMethod = new ServiceMethod(beanObj, method, handler.order(), handler.description());
                        log.info("初始化handler,关键字=[{}],类型=[{}]，order=[{}]", values[i], types[i], handler.order());
                        switch (types[i]) {
                            case HandlerMatchType.COMPLETE, HandlerMatchType.CONTAINS, HandlerMatchType.START, HandlerMatchType.END -> channelGroup.add(new CommandChannel(values[i], serviceMethod, types[i]));
                            case HandlerMatchType.ALL -> allExecuteChannel.add(new ExecuteChannel("all", serviceMethod));
                            default -> {
                                log.error("注册指令时出错，请检查指令类型是否合法");
                                throw new RuntimeException();
                            }
                        }
                    }
                }
            }
            channelGroup.sort(Comparator.comparingInt((Channel o) -> ((CommandChannel) o).type()).thenComparingInt(Channel::order));
            allExecuteChannel.sort(Comparator.comparingInt(Channel::order));
        }
    }

    public void execute(ChannelContext context) {
        for (Channel channel : allExecuteChannel) {
            if (!channel.execute(context)) {
                break;
            }
        }
        for (Channel channel : channelGroup) {
            if (!channel.execute(context)) {
                break;
            }
        }
    }

    public boolean removeCommand(String command) {
        for (int i = 0; i < channelGroup.size(); i++) {
            if (channelGroup.get(i).id().equals(command)) {
                excludeChannel.add(channelGroup.remove(i));
                return true;
            }
        }
        return false;
    }

    public boolean restoreCommand(String command) {
        for (int i = 0; i < excludeChannel.size(); i++) {
            if (excludeChannel.get(i).id().equals(command)) {
                channelGroup.add(excludeChannel.remove(i));
                channelGroup.sort(Comparator.comparingInt((Channel o) -> ((CommandChannel) o).type()).thenComparingInt(Channel::order));
                return true;
            }
        }
        return false;
    }

    public List<String> restoreCommandList() {
        List<String> list = new ArrayList<>(excludeChannel.size());
        for (Channel channel : excludeChannel) {
            list.add(channel.id());
        }
        return list;
    }

    public Map<String, String> commandMap() {
        Map<String, String> map = new HashMap<>(channelGroup.size());
        for (Channel channel : channelGroup) {
            map.put(channel.id(), channel.description());
        }
        return map;
    }

}
