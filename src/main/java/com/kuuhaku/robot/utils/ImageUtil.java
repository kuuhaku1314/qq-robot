package com.kuuhaku.robot.utils;


import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/16 5:55
 * @Description 图片处理相关
 */
public class ImageUtil {

    public final static int WHITE = 0x00ffffff;
    public final static int ALPHA = 0x00ffffff;

    public static void splitImage(String inPath, String outPath) throws Exception {
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
        BufferedImage image= new BufferedImage((int) (width * 1.03), height, BufferedImage.TYPE_INT_RGB);
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
    }

    /**
     * 设置源图片为背景透明，并设置透明度
     * @param srcImage 源图片
     * @param desFile 目标文件
     * @param alpha 透明度
     * @param formatName 文件格式
     * @throws IOException
     */
    public static void transparentImage(BufferedImage srcImage,
                                        String desFile, int alpha, String formatName) throws IOException {
        int imgHeight = srcImage.getHeight();
        int imgWidth = srcImage.getWidth();
        int c = srcImage.getRGB(3, 3);
        //防止越位
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 10) {
            alpha = 10;
        }
        BufferedImage bi = new BufferedImage(imgWidth, imgHeight,
                BufferedImage.TYPE_4BYTE_ABGR);
        for(int i = 0; i < imgWidth; ++i)
        {
            for(int j = 0; j < imgHeight; ++j)
            {
                //把背景设为透明
                if(srcImage.getRGB(i, j) == c){
                    bi.setRGB(i, j, c & 0x00ffffff);
                }
                //设置透明度
                else{
                    int rgb = bi.getRGB(i, j);
                    rgb = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                    bi.setRGB(i, j, rgb);
                }
            }
        }
        ImageIO.write(bi, StringUtils.isEmpty(formatName)?"":formatName, new File(desFile));
    }

    /**
     * 创建任意角度的旋转图像
     * @param image
     * @param theta
     * @param backgroundColor
     * @return
     */
    public BufferedImage rotateImage(BufferedImage image, double theta, Color backgroundColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        // 度转弧度
        double angle = theta * Math.PI / 180;
        double[] xCoords = getX(width / 2, height / 2, angle);
        double[] yCoords = getY(width / 2, height / 2, angle);
        int WIDTH = (int) (xCoords[3] - xCoords[0]);
        int HEIGHT = (int) (yCoords[3] - yCoords[0]);
        BufferedImage resultImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                int x = i - WIDTH / 2;
                int y = HEIGHT / 2 - j;
                double radius = Math.sqrt(x * x + y * y);
                double angle1;
                if (y > 0) {
                    angle1 = Math.acos(x / radius);
                } else {
                    angle1 = 2 * Math.PI - Math.acos(x / radius);
                }
                x = (int) (radius * Math.cos(angle1 - angle));
                y = (int) (radius * Math.sin(angle1 - angle));
                if (x < (width / 2) & x > -(width / 2) & y < (height / 2) & y > -(height / 2)) {
                    int rgb = image.getRGB(x + width / 2, height / 2 - y);
                    resultImage.setRGB(i, j, rgb);
                }else {
                    int rgb = ((0) << 24) | ((backgroundColor.getRed() & 0xff) << 16) | ((backgroundColor.getGreen() & 0xff) << 8)
                            | ((backgroundColor.getBlue() & 0xff));
                    resultImage.setRGB(i, j, rgb);
                }
            }
        }
        return resultImage;
    }

    // 获取四个角点旋转后Y方向坐标
    private double[] getY(int i, int j, double angle) {
        double[] results = new double[4];
        double radius = Math.sqrt(i * i + j * j);
        double angle1 = Math.asin(j / radius);
        results[0] = radius * Math.sin(angle1 + angle);
        results[1] = radius * Math.sin(Math.PI - angle1 + angle);
        results[2] = -results[0];
        results[3] = -results[1];
        Arrays.sort(results);
        return results;
    }

    // 获取四个角点旋转后X方向坐标
    private double[] getX(int i, int j, double angle) {
        double[] results = new double[4];
        double radius = Math.sqrt(i * i + j * j);
        double angle1 = Math.acos(i / radius);
        results[0] = radius * Math.cos(angle1 + angle);
        results[1] = radius * Math.cos(Math.PI - angle1 + angle);
        results[2] = -results[0];
        results[3] = -results[1];
        Arrays.sort(results);
        return results;
    }

}
