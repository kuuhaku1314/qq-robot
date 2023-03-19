package com.kuuhaku.robot.handler;

import com.kuuhaku.robot.common.annotation.Handler;
import com.kuuhaku.robot.common.annotation.HandlerComponent;
import com.kuuhaku.robot.common.annotation.Permission;
import com.kuuhaku.robot.common.constant.HandlerMatchType;
import com.kuuhaku.robot.common.constant.PermissionRank;
import com.kuuhaku.robot.core.chain.ChannelContext;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 0:33
 * @Description 关键词禁言
 */
@HandlerComponent
public class MuteHandler {
    private final Map<String, Set<String>> map = new ConcurrentHashMap<>();


    // 设置了禁言关键词后由这个方法进行禁言
    @Handler(order = -100)
    public void toMute(ChannelContext ctx) {
        MessageEvent event = ctx.event();
        User sender = event.getSender();
        if (map.get(ctx.groupIdStr()) == null) {
            return;
        }
        boolean flag = false;
        for (String s : map.get(ctx.groupIdStr())) {
            if (ctx.command().getMsg().contains(s)) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            return;
        }
        Group group = event.getBot().getGroup(ctx.groupId());
        if (group != null) {
            if (group.getBotPermission().getLevel() == MemberPermission.ADMINISTRATOR.getLevel()) {
                if (sender instanceof NormalMember) {
                    NormalMember normalMember = (NormalMember) sender;
                    if (normalMember.getPermission().getLevel() == MemberPermission.MEMBER.getLevel()) {
                        if (!normalMember.isMuted()) {
                            normalMember.mute(60);
                            Mirai.getInstance().recallMessage(event.getBot(), event.getSource());
                        }
                    }
                }
            }
        }
    }

    @Permission(level = PermissionRank.ADMIN)
    @Handler(values = {"添加禁言关键词"}, types = {HandlerMatchType.START},
            description = "格式如[添加禁言关键词 加群]去添加禁言关键词，需要有管理员权限才能禁言")
    public void addMuteKeyword(ChannelContext ctx) {
        if (ctx.command().isEmpty()) {
            return;
        }
        if (map.get(ctx.groupIdStr()) == null) {
            HashSet<String> set = new HashSet<>(ctx.command().params());
            map.put(ctx.groupIdStr(), set);
        } else {
            map.get(ctx.groupIdStr()).addAll(ctx.command().params());
        }
        ctx.group().sendMessage("添加禁言关键词成功");
    }

    @Permission(level = PermissionRank.ADMIN)
    @Handler(values = {"移除禁言关键词"}, types = {HandlerMatchType.START},
            description = "格式如[添加禁言关键词 加群]去移除已加的禁言关键词")
    public void removeMuteKeyword(ChannelContext ctx) {
        if (ctx.command().isEmpty()) {
            return;
        }
        Set<String> set = map.get(ctx.groupIdStr());
        if (set == null) {
            ctx.group().sendMessage("当前无禁言词");
        } else {
            ctx.command().params().forEach(set::remove);
            ctx.group().sendMessage("移除成功");
        }
    }

    @Handler(values = {"当前禁言关键词"}, types = {HandlerMatchType.COMPLETE},
            description = "当前已加的禁言关键词列表")
    public void muteKeyword(ChannelContext ctx) {
        Set<String> set = map.get(ctx.groupIdStr());
        if (set == null || set.isEmpty()) {
            ctx.group().sendMessage("当前无禁言词");
            return;
        }
        MessageChain result = MessageUtils.newChain();
        result = result.plus("现有禁言词如下:").plus("\n");
        for (String command : set) {
            result = result.plus("关键词:  " + command).plus("\n");
        }
        ctx.group().sendMessage(result);
    }

}
