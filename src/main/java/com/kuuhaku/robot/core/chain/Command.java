package com.kuuhaku.robot.core.chain;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by kuuhaku
 * @Date 2021/4/23 22:49
 * @Description
 */
@Slf4j
public class Command implements ICommand{

    public static final String SEPARATOR = " ";

    private String baseCommand;

    private final String msg;

    private final List<String> params = new ArrayList<>();

    private Command reverseCommand = null;

    public Command(String msg) {
        this(msg, false);
    }

    private Command(String msg, boolean reverse) {
        this.msg = msg;
        parse(reverse);
    }

    @Override
    public String baseCommand() {
        return baseCommand;
    }

    @Override
    public List<String> params() {
        return params;
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    private void parse(boolean reverse) {
        String[] s = msg.split(SEPARATOR);
        if (reverse) {
            if (s.length != 0) {
                baseCommand = s[s.length - 1];
            }
            if (s.length > 1) {
                params.addAll(Arrays.asList(s).subList(0, s.length - 1).stream().
                        filter(StringUtils::isNotEmpty).collect(Collectors.toList()));
            }
        } else {
            if (s.length != 0) {
                baseCommand = s[0];
            }
            if (s.length > 1) {
                params.addAll(Arrays.asList(s).subList(1, s.length).stream().
                        filter(StringUtils::isNotEmpty).collect(Collectors.toList()));
            }
        }
    }

    /**
     * 尾匹配参数和指令翻转，所以获取指令请用这个
     * @return 翻转指令
     */
    Command reverseCommand() {
        if (reverseCommand == null) {
            Command command = new Command(msg, true);
            this.reverseCommand = command;
            return command;
        } else {
            return reverseCommand;
        }
    }

    public Iterator<String> iterator() {
        return params.iterator();
    }

    public int paramSize() {
        return params.size();
    }

    public String getMsg() {
        return msg;
    }
}
