package com.kuuhaku.robot.core.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/12 2:20
 * @Description 发送图片service
 */
@Service
@Slf4j
public class ImageService {

    /**
     * 上传图片
     *
     * @param localPath 本地图片地址
     * @return 图片
     */
    public Image uploadImage(String localPath, MessageEvent event) {
        ContactList<Group> groupList = event.getBot().getGroups();
        Group group = null;
        for (Group groupTemp : groupList) {
            group = groupTemp;
            break;
        }
        if (group == null) {
            return null;
        }
        ExternalResource externalResource = ExternalResource.create(new File(localPath));
        Image image =  group.uploadImage(externalResource);
        try {
            externalResource.inputStream().close();
            externalResource.close();
            log.info("关闭图片流成功");
            return image;
        } catch (IOException e) {
            log.info("关闭图片流出现异常");
            e.printStackTrace();
            return image;
        }
    }

    /**
     * 解释图片为message
     * @param image 图片
     * @return 图片信息
     */
    public MessageChain parseMsgChainByImg(Image image) {
        MessageChain messageChain = MessageUtils.newChain();
        if (image == null) {
            messageChain = messageChain.plus("发送图片失败");
        } else {
            messageChain = messageChain.plus(image);
        }
        return messageChain;
    }

}
