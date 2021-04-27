package com.kuuhaku.robot.handler.unity;

import com.kuuhaku.robot.common.annotation.HandlerComponent;
import net.mamoe.mirai.contact.UserOrBot;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.PokeMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/13 0:00
 * @Description 反戳
 */
@HandlerComponent
public class NudgeHandler {
    public static List<PokeMessage> pokeMsg = new ArrayList<>();
    static {
        pokeMsg.add(PokeMessage.FangDaZhao);
        pokeMsg.add(PokeMessage.BiXin);
        pokeMsg.add(PokeMessage.DianZan);
        pokeMsg.add(PokeMessage.ChuoYiChuo);
        pokeMsg.add(PokeMessage.XinSui);
        pokeMsg.add(PokeMessage.DianZan);
        pokeMsg.add(PokeMessage.GouYin);
    }

    public void toNudge(NudgeEvent event) {
        UserOrBot from = event.getFrom();
        UserOrBot target = event.getTarget();
        if (from.getId() != event.getBot().getId() && target.getId() == event.getBot().getId()) {
            int i = new Random(System.currentTimeMillis()).nextInt(5);
            if (i > 2) {
                int randomNum = new Random(System.currentTimeMillis()).nextInt(pokeMsg.size());
                PokeMessage msg = pokeMsg.get(randomNum);
                event.getSubject().sendMessage(msg);
                return;
            }
            from.nudge().sendTo(event.getSubject());
        }
    }

}
