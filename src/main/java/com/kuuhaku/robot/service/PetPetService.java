package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.ImageService;
import com.kuuhaku.robot.utils.ImageUtil;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/16 7:36
 * @Description 搓头
 */
@Service
@Slf4j
public class PetPetService {

    @Value("${robot.pet.path}")
    private String petPetPath;
    @Value("${robot.pa.path}")
    private String paPath;
    @Value("${robot.diu.path}")
    private String diuPath;
    /**
     * 每帧头像的宽度
     */
    public static int[] iconWidth = {59 * 5, 69 * 5, 77 * 5, 69 * 5, 59 * 5};
    /**
     * 每帧头像的长度
     */
    public static int[] iconHeight = {59 * 5, 54 * 5, 49 * 5, 50 * 5, 63 * 5};
    /**
     * 每帧头像的x坐标
     */
    public static int[] iconX = {27 * 5, 22 * 5, 18 * 5, 22 * 5, 27 * 5};
    /**
     * 每帧头像的y坐标
     */
    public static int[] iconY = {31 * 5, 36 * 5, 41 * 5, 41 * 5, 28 * 5};
    /**
     * 生成gif像素
     */
    public static int imgSize = 114 * 5;
    /**
     * 爬图片路径
     */
    public static ArrayList<String> list = new ArrayList<>();
    public static BufferedImage imgOne;
    public static BufferedImage imgTwo;
    public static BufferedImage imgThree;
    public static BufferedImage imgFour;
    public static BufferedImage imgFive;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private ImageService imageService;

    @PostConstruct
    void init () {
        try {
            list = DownloadService.getImageFiles(paPath);
            InputStream imgOneStream = new FileInputStream(petPetPath + "frame0.png");
            InputStream imgTwoStream = new FileInputStream(petPetPath + "frame1.png");
            InputStream imgThreeStream = new FileInputStream(petPetPath + "frame2.png");
            InputStream imgFourStream = new FileInputStream(petPetPath + "frame3.png");
            InputStream imgFiveStream = new FileInputStream(petPetPath + "frame4.png");
            imgOne = ImageIO.read(imgOneStream);
            imgTwo = ImageIO.read(imgTwoStream);
            imgThree = ImageIO.read(imgThreeStream);
            imgFour = ImageIO.read(imgFourStream);
            imgFive = ImageIO.read(imgFiveStream);
            imgOne = toBufferedImage(imgOne.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH));
            imgTwo = toBufferedImage(imgTwo.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH));
            imgThree = toBufferedImage(imgThree.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH));
            imgFour = toBufferedImage(imgFour.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH));
            imgFive = toBufferedImage(imgFive.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH));
            imgOneStream.close();
            imgTwoStream.close();
            imgThreeStream.close();
            imgFourStream.close();
            imgFiveStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toThrow(String path) {
        InputStream throwImageStream = null;
        InputStream iconStream = null;
        try {
            throwImageStream = new FileInputStream(diuPath);
            BufferedImage throwImage = ImageIO.read(throwImageStream);
            throwImageStream.close();
            throwImageStream = null;
            iconStream = new FileInputStream(path);
            BufferedImage icon = ImageIO.read(iconStream);
            iconStream.close();
            iconStream = null;
            icon = roundImage(icon, icon.getHeight(), icon.getHeight());
            Image iconOne = icon.getScaledInstance(124, 124, Image.SCALE_SMOOTH);
            throwImage.getGraphics().drawImage(iconOne, 20, 192 ,null);
            String outPath = downloadService.getRandomPngPath();
            ImageIO.write(throwImage, "png", new File(outPath));
            return outPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (throwImageStream != null) {
                    throwImageStream.close();
                }
                if (iconStream != null) {
                    iconStream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private String toPa(String path) {
        InputStream paImageStream = null;
        InputStream iconStream = null;
        try {
            int i = new Random(System.currentTimeMillis()).nextInt(list.size());
            paImageStream = new FileInputStream(list.get(i));
            BufferedImage paImage = ImageIO.read(paImageStream);
            paImageStream.close();
            paImageStream = null;
            iconStream = new FileInputStream(path);
            BufferedImage icon = ImageIO.read(iconStream);
            iconStream.close();
            iconStream = null;
            icon = roundImage(icon, icon.getHeight(), icon.getHeight());
            Image iconOne = icon.getScaledInstance(paImage.getWidth() / 5, paImage.getHeight() / 5, Image.SCALE_SMOOTH);
            paImage.getGraphics().drawImage(iconOne, 0, paImage.getHeight() - paImage.getHeight() / 5 ,null);
            String outPath = downloadService.getRandomPngPath();
            ImageIO.write(paImage, "png", new File(outPath));
            return outPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (paImageStream != null) {
                    paImageStream.close();
                }
                if (iconStream != null) {
                    iconStream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    private String toPetPet(String path) {
        InputStream iconStream = null;
        OutputStream out = null;
        try {
            iconStream = new FileInputStream(path);
            BufferedImage icon = ImageIO.read(iconStream);
            iconStream.close();
            iconStream = null;
            icon = roundImage(icon, icon.getHeight(), icon.getWidth());
            Image iconOne = icon.getScaledInstance((int)(iconWidth[0] * 1.2), (int)(iconHeight[0] * 1.2), Image.SCALE_SMOOTH);
            Image iconTwo = icon.getScaledInstance((int)(iconWidth[1] * 1.2), (int)(iconHeight[1] * 1.2), Image.SCALE_SMOOTH);
            Image iconThree = icon.getScaledInstance((int)(iconWidth[2] * 1.2), (int)(iconHeight[2] * 1.2), Image.SCALE_SMOOTH);
            Image iconFour = icon.getScaledInstance((int)(iconWidth[3] * 1.2), (int)(iconHeight[3] * 1.2), Image.SCALE_SMOOTH);
            Image iconFive = icon.getScaledInstance((int)(iconWidth[4] * 1.2), (int)(iconHeight[4] * 1.2), Image.SCALE_SMOOTH);
            BufferedImage imageOne = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
            BufferedImage imageTwo = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
            BufferedImage imageThree = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
            BufferedImage imageFour = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
            BufferedImage imageFive = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
            imageOne.getGraphics().drawImage(iconOne, iconX[0], iconY[0], null);
            imageTwo.getGraphics().drawImage(iconTwo, iconX[1], iconY[1], null);
            imageThree.getGraphics().drawImage(iconThree, iconX[2], iconY[2], null);
            imageFour.getGraphics().drawImage(iconFour, iconX[3], iconY[3], null);
            imageFive.getGraphics().drawImage(iconFive, iconX[4], iconY[4], null);
            imageOne.getGraphics().drawImage(imgOne, 0, 0, null);
            imageTwo.getGraphics().drawImage(imgTwo, 0, 0, null);
            imageThree.getGraphics().drawImage(imgThree, 0, 0, null);
            imageFour.getGraphics().drawImage(imgFour, 0, 0, null);
            imageFive.getGraphics().drawImage(imgFive, 0, 0, null);
            String outPath = downloadService.getRandomPath() + ".gif";
            out = new FileOutputStream(outPath);
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(out);
            encoder.setRepeat(0);
            encoder.setDelay(40);
            encoder.addFrame(imageOne);
            encoder.addFrame(imageTwo);
            encoder.addFrame(imageThree);
            encoder.addFrame(imageFour);
            encoder.addFrame(imageFive);
            encoder.finish();
            out.close();
            out = null;
            return outPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (iconStream != null) {
                    iconStream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private String toRipped(String path) {
        String randomPath = downloadService.getRandomPath();
        try {
            ImageUtil.splitImage(path, randomPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return randomPath;
    }


    private BufferedImage toBufferedImage(Image img) {
        BufferedImage bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufImg .createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bufImg;
    }

    private BufferedImage roundImage(BufferedImage image, int targetSize, int cornerRadius) {
        BufferedImage outputImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = outputImage.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, targetSize, targetSize, cornerRadius, cornerRadius));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return outputImage;
    }

    public MessageChain getPetPet(MessageEvent messageEvent, String memberId, Group group) {
        log.info("搓头获取的at用户id=[{}]", memberId);
        NormalMember member = group.get(Long.parseLong(memberId));
        if (member == null) {
            return null;
        }
        MessageChain messageChain = MessageUtils.newChain();
        String avatarUrl = messageEvent.getBot().getId() == member.getId() ?
                messageEvent.getSender().getAvatarUrl() : member.getAvatarUrl();
        String path = downloadService.getRandomPngPath();
        downloadService.download(avatarUrl, path);
        String petPet = toPetPet(path);
        if (petPet == null) {
            downloadService.deleteFile(path);
            return null;
        }
        net.mamoe.mirai.message.data.Image image = imageService.uploadImage(petPet, messageEvent);
        MessageChain imageMessage = imageService.parseMsgChainByImg(image);
        messageChain = messageChain.plus(imageMessage);
        downloadService.deleteFile(path);
        downloadService.deleteFile(petPet);
        return messageChain;
    }

    public MessageChain getRipped(MessageEvent messageEvent, String memberId, Group group) {
        log.info("裂开获取的at用户id=[{}]", memberId);
        NormalMember member = group.get(Long.parseLong(memberId));
        if (member == null) {
            return null;
        }
        MessageChain messageChain = MessageUtils.newChain();
        String avatarUrl = messageEvent.getBot().getId() == member.getId() ?
                messageEvent.getSender().getAvatarUrl() : member.getAvatarUrl();
        String path = downloadService.getRandomPngPath();
        downloadService.download(avatarUrl, path);
        String si = toRipped(path);
        if (si == null) {
            downloadService.deleteFile(path);
            return null;
        }
        net.mamoe.mirai.message.data.Image image = imageService.uploadImage(si, messageEvent);
        MessageChain imageMessage = imageService.parseMsgChainByImg(image);
        messageChain = messageChain.plus(imageMessage);
        downloadService.deleteFile(path);
        downloadService.deleteFile(si);
        return messageChain;
    }

    public MessageChain getPa(MessageEvent messageEvent, String memberId, Group group) {
        log.info("爬获取的at用户id=[{}]", memberId);
        NormalMember member = group.get(Long.parseLong(memberId));
        if (member == null) {
            return null;
        }
        MessageChain messageChain = MessageUtils.newChain();
        String avatarUrl = messageEvent.getBot().getId() == member.getId() ?
                messageEvent.getSender().getAvatarUrl() : member.getAvatarUrl();
        String path = downloadService.getRandomPngPath();
        downloadService.download(avatarUrl, path);
        String pa = toPa(path);
        if (pa == null) {
            downloadService.deleteFile(path);
            return null;
        }
        net.mamoe.mirai.message.data.Image image = imageService.uploadImage(pa, messageEvent);
        MessageChain imageMessage = imageService.parseMsgChainByImg(image);
        messageChain = messageChain.plus(imageMessage);
        downloadService.deleteFile(path);
        downloadService.deleteFile(pa);
        return messageChain;
    }

    public MessageChain getDiu(MessageEvent messageEvent, String memberId, Group group) {
        log.info("丢获取的at用户id=[{}]", memberId);
        NormalMember member = group.get(Long.parseLong(memberId));
        if (member == null) {
            return null;
        }
        MessageChain messageChain = MessageUtils.newChain();
        String avatarUrl = messageEvent.getBot().getId() == member.getId() ?
                messageEvent.getSender().getAvatarUrl() : member.getAvatarUrl();
        String path = downloadService.getRandomPngPath();
        downloadService.download(avatarUrl, path);
        String diu = toThrow(path);
        if (diu == null) {
            downloadService.deleteFile(path);
            return null;
        }
        net.mamoe.mirai.message.data.Image image = imageService.uploadImage(diu, messageEvent);
        MessageChain imageMessage = imageService.parseMsgChainByImg(image);
        messageChain = messageChain.plus(imageMessage);
        downloadService.deleteFile(path);
        downloadService.deleteFile(diu);
        return messageChain;
    }


    public static void main(String[] args) {
        new PetPetService().toPetPet("D:\\image\\PyPetPet\\111.png");
    }
}
