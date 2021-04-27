package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.service.PetPetService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/16 8:29
 * @Description 摸头等
 */
@HandlerComponent
@Slf4j
public class PetPetHandler {
    @Autowired
    private PetPetService petPetService;

    @Permission
    @Handler(values = {"搓"}, types = {HandlerMatchType.END})
    public void toPetPet(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        log.info("进入搓头");
        MessageChain petPet = petPetService.getPetPet(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (petPet != null) {
            ctx.group().sendMessage(petPet);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

    @Permission
    @Handler(values = {"裂开"}, types = {HandlerMatchType.END})
    public void toRipped(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        log.info("进入裂开");
        MessageChain ripped = petPetService.getRipped(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (ripped != null) {
            ctx.group().sendMessage(ripped);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

    @Permission
    @Handler(values = {"爬"}, types = {HandlerMatchType.END})
    public void toPa(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        log.info("进入爬");
        MessageChain pa = petPetService.getPa(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (pa != null) {
            ctx.group().sendMessage(pa);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

    @Permission
    @Handler(values = {"丢"}, types = {HandlerMatchType.END})
    public void toDiu(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        log.info("进入丢");
        MessageChain diu = petPetService.getDiu(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (diu != null) {
            ctx.group().sendMessage(diu);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

}
