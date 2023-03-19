package com.kuuhaku.robot.utils;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author by kuuhaku
 * @Date 2021/2/16 5:55
 * @Description 图片处理相关
 */
public class ImageUtil {

    /**
     * 设置源图片为背景透明，并设置透明度
     *
     * @param srcImage   源图片
     * @param desFile    目标文件
     * @param alpha      透明度
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
        for (int i = 0; i < imgWidth; ++i) {
            for (int j = 0; j < imgHeight; ++j) {
                //把背景设为透明
                if (srcImage.getRGB(i, j) == c) {
                    bi.setRGB(i, j, c & 0x00ffffff);
                }
                //设置透明度
                else {
                    int rgb = bi.getRGB(i, j);
                    rgb = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                    bi.setRGB(i, j, rgb);
                }
            }
        }
        ImageIO.write(bi, StringUtils.isEmpty(formatName) ? "" : formatName, new File(desFile));
    }

    /**
     * 创建任意角度的旋转图像
     *
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
                } else {
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

    private static final int QRCOLOR = 0xFF000000; // 默认是黑色
    private static final int BGWHITE = 0xFFFFFFFF; // 背景颜色

    private static final int WIDTH = 200; // 二维码宽
    private static final int HEIGHT = 200; // 二维码高

    // 用于设置QR二维码参数
    private static Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {
        private static final long serialVersionUID = 1L;

        {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);// 设置QR二维码的纠错级别（H为最高级别）具体级别信息
            put(EncodeHintType.CHARACTER_SET, "utf-8");// 设置编码方式
            put(EncodeHintType.MARGIN, 0);
        }
    };


    // 生成带logo的二维码图片

    /***
     *@param logoFile  logo图地址
     * @param codeFile  二维码生成地址
     * @param qrUrl 扫描二维码方位地址
     * */
    public static void drawLogoQRCode(File logoFile, File codeFile, String qrUrl) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

            // 开始利用二维码数据创建Bitmap图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
                }
            }

            int width = image.getWidth();
            int height = image.getHeight();
            if (Objects.nonNull(logoFile) && logoFile.exists()) {
                // 构建绘图对象
                Graphics2D g = image.createGraphics();
                // 读取Logo图片
                BufferedImage logo = ImageIO.read(logoFile);
                // 开始绘制logo图片
                g.drawImage(logo, width * 2 / 5, height * 2 / 5, width * 2 / 10, height * 2 / 10, null);
                g.dispose();
                logo.flush();
            }

            image.flush();

            ImageIO.write(image, "png", codeFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //头像logo
        File logoFile = new File("C://Users//Administrator//Desktop//2.jpg");
        //生成二维码地址
        File QrCodeFile = new File("C://Users//Administrator//Desktop//test.jpg");
        String url = "https://www.baidu.com/";
        drawLogoQRCode(logoFile, QrCodeFile, url);

        mergeImage("C://Users//Administrator//Desktop//1.jpg", "C://Users//Administrator//Desktop//test.jpg", "C://Users//Administrator//Desktop//test2.jpg", "63", "163");
    }


    /***
     * 二维码嵌套背景图的方法
     *@param bigPath 背景图 - 可传网络地址
     *@param smallPath 二维码地址 - 可传网络地址
     *@param newFilePath 生成新图片的地址
     * @param  x 二维码x坐标
     *  @param  y 二维码y坐标
     * */
    public static void mergeImage(String bigPath, String smallPath, String newFilePath, String x, String y) throws IOException {

        try {
            BufferedImage small;
            BufferedImage big;
            if (bigPath.contains("http://") || bigPath.contains("https://")) {
                URL url = new URL(bigPath);
                big = ImageIO.read(url);
            } else {
                big = ImageIO.read(new File(bigPath));
            }


            if (smallPath.contains("http://") || smallPath.contains("https://")) {

                URL url = new URL(smallPath);
                small = ImageIO.read(url);
            } else {
                small = ImageIO.read(new File(smallPath));
            }

            Graphics2D g = big.createGraphics();

            float fx = Float.parseFloat(x);
            float fy = Float.parseFloat(y);
            int x_i = (int) fx;
            int y_i = (int) fy;
            g.drawImage(small, x_i, y_i, small.getWidth(), small.getHeight(), null);
            g.dispose();
            ImageIO.write(big, "png", new File(newFilePath));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
