package com.kuuhaku.robot.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/16 3:24
 * @Description 文字转图片工具，防屏蔽
 */
public class MojiUtil {

    /**
     * 注意路径的文件格式jpg
     * @param moji  待转换字符串
     * @param path 文件路径
     * @return 文件路径
     */
    public static String createImage(String moji, String path) {
        String[] strArr = moji.split("\n");
        // 每张图片的高度，请确保能容纳下所有文字，不然会丢失第一页内容
        int imageHeight = 800;
        // 每行或者每个文字的高度
        int lineHeight = 30;
        // 每张图片有多少行文字
        int everyLine = imageHeight / lineHeight;
        try {
            // 注意字体选对中文支持的字体
            createImage(strArr, path, new Font("宋体", Font.PLAIN, 22), 550, imageHeight,  everyLine, lineHeight);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 根据str,font的样式等生成图片
     * https://blog.csdn.net/sinat_28505133/article/details/54669111
     *
     * @param strArr
     * @param font
     * @param width
     * @param imageHeight
     * @throws Exception
     */
    private static void createImage(String[] strArr, String path, Font font,
                                   int width, int imageHeight, int everyLine, int lineHeight) throws Exception {

        FontMetrics fm = sun.font.FontDesignMetrics.getMetrics(font);
        // 标点符号也算一个字
        int stringWidth = fm.charWidth('字');
        int lineStringNum = width % stringWidth == 0 ? (width / stringWidth) : (width / stringWidth) + 1;

        List<String> newList = new ArrayList<String>();
        List<String> listStr = new ArrayList<String>(Arrays.asList(strArr));
        for (int j = 0; j < listStr.size(); j++) {
            if( listStr.get(j).length() > lineStringNum){
                newList.add(listStr.get(j).substring(0, lineStringNum));
                listStr.add(j + 1, listStr.get(j).substring(lineStringNum));
                listStr.set(j,listStr.get(j).substring(0, lineStringNum));
            }else{
                newList.add(listStr.get(j));
            }
        }

        int a = newList.size();
        int b = everyLine;
        int imgNum = a % b == 0 ? (a / b) : (a / b) + 1;

        for (int m = 0; m < imgNum; m++) {
            File outFile = new File(path);
            // 创建图片
            BufferedImage image = new BufferedImage(width, imageHeight,
                    BufferedImage.TYPE_INT_BGR);
            Graphics g = image.getGraphics();
            g.setClip(0, 0, width, imageHeight);
            // 背景色黑色
            g.setColor(Color.black);
            g.fillRect(0, 0, width, imageHeight);
            // 字体颜色白色
            g.setColor(Color.white);
            // 设置画笔字体
            g.setFont(font);
            // 每张多少行，当到最后一张时判断是否填充满
            for (int i = 0; i < everyLine; i++) {
                int index = i + m * everyLine;
                if (newList.size() - 1 >= index) {
                    g.drawString(newList.get(index), 0, lineHeight * (i + 1));
                }
            }
            g.dispose();
            // 输出png图片
            ImageIO.write(image, "png", outFile);
        }
    }

}


