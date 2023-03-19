package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.ImageService;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.apache.commons.lang3.RandomUtils;
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
 * @Author by kuuhaku
 * @Date 2021/2/16 7:36
 * @Description 搓头
 */
@Service
@Slf4j
public class PetPetService {

    public final static int WHITE = 0x00ffffff;
    public final static int ALPHA = 0x00ffffff;
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
    public static ArrayList<BufferedImage> imageList = new ArrayList<>();
    @Value("${robot.pet.path}")
    private String petPetPath;
    @Value("${robot.pa.path}")
    private String paPath;
    @Value("${robot.diu.path}")
    private String diuPath;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private ImageService imageService;

    @PostConstruct
    void init() {
        list = DownloadService.getImageFiles(paPath);
        for (int i = 0; i < 5; i++) {
            BufferedImage image;
            try (var imgStream = new FileInputStream(petPetPath + "frame" + i + ".png")) {
                image = ImageIO.read(imgStream);
                imageList.add(toBufferedImage(image.getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String toThrow(String path) {

        try (
                InputStream throwImageStream = new FileInputStream(diuPath);
                InputStream iconStream = new FileInputStream(path)
        ) {
            BufferedImage throwImage = ImageIO.read(throwImageStream);
            BufferedImage icon = ImageIO.read(iconStream);
            icon = roundImage(icon, icon.getHeight(), icon.getHeight());
            Image iconOne = icon.getScaledInstance(124, 124, Image.SCALE_SMOOTH);
            throwImage.getGraphics().drawImage(iconOne, 20, 192, null);
            String outPath = downloadService.getRandomPngPath();
            ImageIO.write(throwImage, "png", new File(outPath));
            return outPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String toPa(String path) {
        try (
                InputStream paImageStream = new FileInputStream(list.get(RandomUtils.nextInt(0, list.size())));
                InputStream iconStream = new FileInputStream(path)
        ) {
            BufferedImage paImage = ImageIO.read(paImageStream);
            BufferedImage icon = ImageIO.read(iconStream);
            icon = roundImage(icon, icon.getHeight(), icon.getHeight());
            Image iconOne = icon.getScaledInstance(paImage.getWidth() / 5, paImage.getHeight() / 5, Image.SCALE_SMOOTH);
            paImage.getGraphics().drawImage(iconOne, 0, paImage.getHeight() - paImage.getHeight() / 5, null);
            String outPath = downloadService.getRandomPngPath();
            ImageIO.write(paImage, "png", new File(outPath));
            return outPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private String toPetPet(String path) {
        String outPath = downloadService.getRandomPath() + ".gif";
        try (
                var iconStream = new FileInputStream(path);
                var out = new FileOutputStream(outPath)
        ) {
            var icon = ImageIO.read(iconStream);
            icon = roundImage(icon, icon.getHeight(), icon.getWidth());
            var encoder = new AnimatedGifEncoder();
            encoder.start(out);
            encoder.setRepeat(0);
            encoder.setDelay(40);
            for (int i = 0; i < iconWidth.length; i++) {
                var image = icon.getScaledInstance((int) (iconWidth[i] * 1.2), (int) (iconHeight[i] * 1.2), Image.SCALE_SMOOTH);
                var bufferedImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
                bufferedImage.getGraphics().drawImage(image, iconX[i], iconY[i], null);
                bufferedImage.getGraphics().drawImage(imageList.get(i), 0, 0, null);
                encoder.addFrame(bufferedImage);
            }
            encoder.finish();
            return outPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String toRipped(String inPath) {
        String outPath = downloadService.getRandomPath();
        try {
            BufferedImage bufferedImageOne = ImageIO.read(new File(inPath));
            BufferedImage bufferedImageTwo = ImageIO.read(new File(inPath));
            int width = bufferedImageOne.getWidth();
            int height = bufferedImageOne.getHeight();
            int medium = width / 2;
            // 边缘混淆
            int[] randomX = new Random(System.currentTimeMillis()).ints(0, 2).limit(width).toArray();
            int[] randomY = new Random(System.currentTimeMillis()).ints(0, 2).limit(height).toArray();
            for (int i = 0; i < width; i++) {
                if (randomX[i] == 1) {
                    bufferedImageOne.setRGB(i, 0, ALPHA);
                    bufferedImageTwo.setRGB(i, 0, ALPHA);
                }
                if (randomX[width - i - 1] == 1) {
                    bufferedImageOne.setRGB(i, height - 1, ALPHA);
                    bufferedImageTwo.setRGB(i, height - 1, ALPHA);
                }
            }
            for (int i = 0; i < height; i++) {
                if (randomY[i] == 1) {
                    bufferedImageOne.setRGB(0, i, ALPHA);
                    bufferedImageTwo.setRGB(0, i, ALPHA);
                }
                if (randomY[height - 1 - i] == 1) {
                    bufferedImageOne.setRGB(width - 1, i, ALPHA);
                    bufferedImageTwo.setRGB(width - 1, i, ALPHA);
                }
            }

            // 分割中心线，x坐标数组
            int[] ints = new Random(System.currentTimeMillis()).ints(0, 2).limit(height).toArray();
            int[] xArray = new int[height];
            for (int i = 0; i < height; i++) {
                if (ints[i] == 1) {
                    medium++;
                } else {
                    medium--;
                }
                xArray[i] = medium;
            }
            BufferedImage image = new BufferedImage((int) (width * 1.03), height, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < (int) (width * 1.03); i++) {
                for (int j = 0; j < height; j++) {
                    image.setRGB(i, j, WHITE);
                }
            }

            // 以中心线分割成两张图片
            for (int i = 0; i < height; i++) {
                for (int j = xArray[i]; j < width; j++) {
                    image.setRGB(j + (int) (width * 0.03), i, bufferedImageOne.getRGB(j, i));
                }
            }
            for (int i = 0; i < height; i++) {
                for (int j = xArray[i]; j >= 0; j--) {
                    image.setRGB(j, i, bufferedImageOne.getRGB(j, i));
                }
            }
            ImageIO.write(image, "png", new File(outPath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outPath;
    }


    private BufferedImage toBufferedImage(Image img) {
        BufferedImage bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufImg.createGraphics();
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

    public void reverseGIF(MessageEvent messageEvent) {
        MessageChain messageChain = MessageUtils.newChain();
        net.mamoe.mirai.message.data.Image outImage = messageEvent.getMessage().get(net.mamoe.mirai.message.data.Image.Key);
        if (outImage == null) {
            return;
        }
        // 小于300kb不复读
        if (outImage.getSize() < 1024 * 300) {
            return;
        }
        String path = downloadService.getRandomPath() + ".gif";
        downloadService.download(net.mamoe.mirai.message.data.Image.queryUrl(outImage), path);

        GifDecoder decoder = new GifDecoder();
        int result = decoder.read(path);
        if (result != 0) {
            downloadService.deleteFile(path);
            return;
        }
        int total = decoder.getFrameCount();
        if (total < 5) {
            return;
        }
        String outPath = downloadService.getRandomPath() + ".gif";
        OutputStream out;
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        try {
            out = new FileOutputStream(outPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            messageChain = messageChain.plus("读取gif出错");
            messageEvent.getSubject().sendMessage(messageChain);
            downloadService.deleteFile(path);
            return;
        }
        encoder.start(out);
        encoder.setRepeat(decoder.getLoopCount());

        for (int i = total - 1; i >= 0; i--) {
            encoder.setDelay(decoder.getDelay(i));
            encoder.addFrame(decoder.getFrame(i));
        }
        encoder.finish();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            messageChain = messageChain.plus("读取gif出错");
            messageEvent.getSubject().sendMessage(messageChain);
            downloadService.deleteFile(path);
            downloadService.deleteFile(outPath);
            return;
        }
        net.mamoe.mirai.message.data.Image image = imageService.uploadImage(outPath, messageEvent);
        MessageChain imageMessage = imageService.parseMsgChainByImg(image);
        messageChain = messageChain.plus(imageMessage);
        downloadService.deleteFile(path);
        downloadService.deleteFile(outPath);
        messageEvent.getSubject().sendMessage(messageChain);
    }

    public void fastGIF(MessageEvent messageEvent) {
        MessageChain messageChain = MessageUtils.newChain();
        net.mamoe.mirai.message.data.Image outImage = messageEvent.getMessage().get(net.mamoe.mirai.message.data.Image.Key);
        if (outImage == null) {
            return;
        }
        String path = downloadService.getRandomPath() + ".gif";
        downloadService.download(net.mamoe.mirai.message.data.Image.queryUrl(outImage), path);

        GifDecoder decoder = new GifDecoder();
        int result = decoder.read(path);
        if (result != 0) {
            downloadService.deleteFile(path);
            return;
        }
        int total = decoder.getFrameCount();
        String outPath = downloadService.getRandomPath() + ".gif";
        OutputStream out;
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        try {
            out = new FileOutputStream(outPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            messageChain = messageChain.plus("读取gif出错");
            messageEvent.getSubject().sendMessage(messageChain);
            downloadService.deleteFile(path);
            return;
        }
        encoder.start(out);
        encoder.setRepeat(decoder.getLoopCount());

        for (int i = 0; i <= total - 1; i++) {
            encoder.setDelay(decoder.getDelay(i) / 2);
            encoder.addFrame(decoder.getFrame(i));
        }
        encoder.finish();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            messageChain = messageChain.plus("读取gif出错");
            messageEvent.getSubject().sendMessage(messageChain);
            downloadService.deleteFile(path);
            downloadService.deleteFile(outPath);
            return;
        }
        net.mamoe.mirai.message.data.Image image = imageService.uploadImage(outPath, messageEvent);
        MessageChain imageMessage = imageService.parseMsgChainByImg(image);
        messageChain = messageChain.plus(imageMessage);
        downloadService.deleteFile(path);
        downloadService.deleteFile(outPath);
        messageEvent.getSubject().sendMessage(messageChain);
    }

}
