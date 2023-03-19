package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.common.constant.PermissionRank;
import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.CommandService;
import com.kuuhaku.robot.core.service.PermissionService;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author by kuuhaku
 * @Date 2021/2/17 11:35
 * @Description 控制权限
 */
@HandlerComponent
public class PermissionHandler {
    @Autowired
    private CommandService commandService;
    @Autowired
    private PermissionService permissionService;

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"添加临时权限"}, types = {HandlerMatchType.END}, description = "给某人添加admin权限，格式如[@123456 添加临时权限]")
    public void addPermission(ChannelContext ctx) {
        permissionService.addAdmin(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("添加成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"解除临时权限"}, types = {HandlerMatchType.END}, description = "移除某人Admin权限，格式如[@123456 解除临时权限]")
    public void deletePermission(ChannelContext ctx) {
        permissionService.removeAdmin(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("解除成功");
    }


    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"关闭使用"}, types = {HandlerMatchType.END}, description = "移除某人使用资格，格式如[@123456 关闭使用]")
    public void closePermission(ChannelContext ctx) {
        permissionService.addMinus(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("关闭成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"开启使用"}, types = {HandlerMatchType.END}, description = "恢复某人使用资格，格式如[@123456 开启使用]")
    public void openPermission(ChannelContext ctx) {
        permissionService.removeMinus(Long.parseLong(ctx.reverseCommand().params().get(0).substring(1)));
        ctx.group().sendMessage("开启成功");
    }


    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"关闭服务"}, types = {HandlerMatchType.COMPLETE}, description = "关闭所有服务")
    public void closeService(ChannelContext ctx) {
        permissionService.addExclusionGroup(ctx.groupId());
        ctx.group().sendMessage("关闭服务成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"开启服务"}, types = {HandlerMatchType.COMPLETE}, description = "开启所有服务")
    public void openService(ChannelContext ctx) {
        permissionService.removeExclusionGroup(ctx.groupId());
        ctx.group().sendMessage("开启服务成功");
    }

    @Permission(level = PermissionRank.MASTER)
    @Handler(values = {"移除指令"}, types = {HandlerMatchType.START}, description = "移除某个指令，如[移除指令 点歌]")
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
    @Handler(values = {"恢复指令"}, types = {HandlerMatchType.START}, description = "恢复某个指令，如[恢复指令 点歌]")
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

    @Handler(values = {"移除指令列表"}, types = {HandlerMatchType.COMPLETE}, description = "当前移除的指令列表")
    public void restoreCommandList(ChannelContext ctx) {
        List<String> list = commandService.restoreCommandList();
        if (list.isEmpty()) {
            ctx.group().sendMessage("现无移除的指令");
        } else {
            MessageChain result = MessageUtils.newChain();
            result = result.plus("现有已移除指令如下:").plus("\n");
            for (String command : list) {
                result = result.plus("指令[" + command + "]").plus("\n");
            }
            ctx.group().sendMessage(result);
        }
    }

    @Handler(values = {"帮助"}, types = {HandlerMatchType.COMPLETE}, description = "给予你帮助")
    public void commandList(ChannelContext ctx) {
        Map<String, String> map = commandService.commandMap();
        StringBuilder result = new StringBuilder();
        result.append("现有指令如下:").append("\n");
        int i = 0;
        int onceMessageLength = 17;
        List<String> msgList = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.append("指令[").append(entry.getKey()).append("]").append("\n").append("用法[").append(entry.getValue()).append("]").append("\n").append("\n");
            if (++i % onceMessageLength == 0) {
                msgList.add(result.toString());
                result = new StringBuilder();
            }
        }
        // 补发最后一次消息
        if (i % onceMessageLength != 0) {
            msgList.add(result.toString());
        }

        ForwardMessage.Node[] list = new ForwardMessage.Node[msgList.size()];
        int timeMillis = (int) (System.currentTimeMillis() / 1000);
        int k = 0;
        for (String msg : msgList) {
            ForwardMessage.Node node = new ForwardMessage.Node(ctx.event().getBot().getId(), timeMillis, ctx.event().getBot().getNick(), MessageUtils.newChain().plus(msg));
            list[k] = node;
            k++;
        }

        List<String> previewList = new ArrayList<>();
        int num = 0;
        for (ForwardMessage.Node node : list) {
            if (num < 3) {
                previewList.add(node.getSenderName() + ":" + node.getMessageChain().contentToString());
                num++;
            } else {
                break;
            }
        }
        String title = "群聊的聊天记录";
        String brief = "[聊天记录]";
        String source = "聊天记录";
        String summary = "查看转发消息";
        ForwardMessage forwardMessage = new ForwardMessage(previewList, title, brief, source, summary, Arrays.asList(list));
        ctx.group().sendMessage(forwardMessage);
    }

}
