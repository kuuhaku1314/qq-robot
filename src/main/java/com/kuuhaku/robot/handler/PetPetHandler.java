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
 * @Author by kuuhaku
 * @Date 2021/2/16 8:29
 * @Description 摸头等
 */
@HandlerComponent
@Slf4j
public class PetPetHandler {
    @Autowired
    private PetPetService petPetService;

    @Permission
    @Handler(values = {"搓"}, types = {HandlerMatchType.END}, description = "发送搓头图片，格式如[@123456 搓]")
    public void toPetPet(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        MessageChain petPet = petPetService.getPetPet(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (petPet != null) {
            ctx.group().sendMessage(petPet);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

    @Permission
    @Handler(values = {"裂开"}, types = {HandlerMatchType.END}, description = "发送裂开图片，格式如[@123456 裂开]")
    public void toRipped(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        MessageChain ripped = petPetService.getRipped(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (ripped != null) {
            ctx.group().sendMessage(ripped);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

    @Permission
    @Handler(values = {"爬"}, types = {HandlerMatchType.END}, description = "发送爬图片，格式如[@123456 爬]")
    public void toPa(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        MessageChain pa = petPetService.getPa(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (pa != null) {
            ctx.group().sendMessage(pa);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

    @Permission
    @Handler(values = {"丢"}, types = {HandlerMatchType.END}, description = "发送丢图片，格式如[@123456 丢]")
    public void toDiu(ChannelContext ctx) {
        List<String> params = ctx.reverseCommand().params();
        if (params.isEmpty() || !StringUtils.isNumeric(params.get(0).substring(1))) {
            return;
        }
        MessageChain diu = petPetService.getDiu(ctx.event(), params.get(0).substring(1), (Group) ctx.group());
        if (diu != null) {
            ctx.group().sendMessage(diu);
        } else {
            ctx.group().sendMessage("指令或内部出错");
        }
    }

}
