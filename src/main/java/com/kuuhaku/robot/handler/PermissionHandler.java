package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.common.constant.PermissionRank;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.CommandService;
import com.kuuhaku.robot.core.service.PermissionService;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/17 11:35
 * @Description 控制权限
 */
@HandlerComponent
public class PermissionHandler {
    @Autowired
    private CommandService commandService;
    @Autowired
    private PermissionService permissionService;

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"添加临时权限"}, types = {HandlerMatchType.END})
    public void addPermission(ChannelContext ctx) {
        permissionService.addAdmin(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("添加成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"解除临时权限"}, types = {HandlerMatchType.END})
    public void deletePermission(ChannelContext ctx) {
        permissionService.removeAdmin(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("解除成功");
    }


    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"关闭使用"}, types = {HandlerMatchType.END})
    public void closePermission(ChannelContext ctx) {
        permissionService.addMinus(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("关闭成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"开启使用"}, types = {HandlerMatchType.END})
    public void openPermission(ChannelContext ctx) {
        permissionService.removeMinus(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("开启成功");
    }


    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"关闭服务"}, types = {HandlerMatchType.COMPLETE})
    public void closeService(ChannelContext ctx) {
        permissionService.addExclusionGroup(ctx.groupId());
        ctx.group().sendMessage("关闭服务成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"开启服务"}, types = {HandlerMatchType.COMPLETE})
    public void openService(ChannelContext ctx) {
        permissionService.removeExclusionGroup(ctx.groupId());
        ctx.group().sendMessage("开启服务成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"移除指令"}, types = {HandlerMatchType.START})
    public void removeCommand(ChannelContext ctx) {
        if (ctx.command().isEmpty()) {
            return;
        }
        String command = ctx.command().params().get(0);
        if ("移除指令".equals(command) || "恢复指令".equals(command)) {
            ctx.group().sendMessage("基础指令不能移除");
            return;
        }
        if (commandService.removeCommand(command)) {
            ctx.group().sendMessage("移除指令成功");
        } else {
            ctx.group().sendMessage("移除指令失败");
        }
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"恢复指令"}, types = {HandlerMatchType.START})
    public void restoreCommand(ChannelContext ctx) {
        if (ctx.command().isEmpty()) {
            return;
        }
        if (commandService.restoreCommand(ctx.command().params().get(0))) {
            ctx.group().sendMessage("恢复指令成功");
        } else {
            ctx.group().sendMessage("恢复指令失败");
        }
    }

    @Handler(values = {"移除指令列表"}, types = {HandlerMatchType.COMPLETE})
    public void restoreCommandList(ChannelContext ctx) {
        List<String> list = commandService.restoreCommandList();
        if (list.isEmpty()) {
            ctx.group().sendMessage("现无移除的指令");
        } else {
            MessageChain result = MessageUtils.newChain();
            result = result.plus("现有已移除指令如下:").plus("\n");
            for (String command : list) {
                result = result.plus("指令:  " + command).plus("\n");
            }
            ctx.group().sendMessage(result);
        }
    }

    @Handler(values = {"指令列表"}, types = {HandlerMatchType.COMPLETE})
    public void commandList(ChannelContext ctx) {
        List<String> list = commandService.commandList();
        MessageChain result = MessageUtils.newChain();
        result = result.plus("现有指令如下:").plus("\n");
        for (String command : list) {
            result = result.plus("指令:  " + command).plus("\n");
        }
        ctx.group().sendMessage(result);
    }

}
