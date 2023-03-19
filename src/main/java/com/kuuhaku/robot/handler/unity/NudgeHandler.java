package com.kuuhaku.robot.handler.unity;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.TimeTaskService;
import com.kuuhaku.robot.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.UserOrBot;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.PokeMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 0:00
 * @Description 反戳
 */
@HandlerComponent
@Slf4j
public class NudgeHandler {
    public final static List<PokeMessage> pokeMsg = new ArrayList<>();

    static {
        pokeMsg.add(PokeMessage.FangDaZhao);
        pokeMsg.add(PokeMessage.BiXin);
        pokeMsg.add(PokeMessage.DianZan);
        pokeMsg.add(PokeMessage.ChuoYiChuo);
        pokeMsg.add(PokeMessage.XinSui);
        pokeMsg.add(PokeMessage.DianZan);
        pokeMsg.add(PokeMessage.GouYin);
    }

    @Autowired
    private TimeTaskService timeTaskService;

    public void toNudge(NudgeEvent event) {
        UserOrBot from = event.getFrom();
        UserOrBot target = event.getTarget();
        if (from.getId() != event.getBot().getId() && target.getId() == event.getBot().getId()) {
            if (RandomUtil.isPass(40)) {
                int randomNum = RandomUtil.random(pokeMsg.size());
                PokeMessage msg = pokeMsg.get(randomNum);
                event.getSubject().sendMessage(msg);
                return;
            }
            from.nudge().sendTo(event.getSubject());
        }
    }

    @Permission
    @Handler(values = {"戳"}, types = {HandlerMatchType.END}, order = -200, description = "[@123456 搓一搓], 机器人帮你戳某个人好多下")
    public void autoNudge(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }

        String id = params.get(0).substring(1);
        Group group = (Group) ctx.group();
        NormalMember member = group.get(Long.parseLong(id));
        if (member != null) {
            Runnable runnable = () -> member.nudge().sendTo(group);
            timeTaskService.submitTask(runnable, 5, TimeUnit.SECONDS);
            timeTaskService.submitTask(runnable, 20, TimeUnit.SECONDS);
            timeTaskService.submitTask(runnable, 35, TimeUnit.SECONDS);
            timeTaskService.submitTask(runnable, 50, TimeUnit.SECONDS);
        }
    }

}
