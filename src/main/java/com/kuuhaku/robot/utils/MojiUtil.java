package com.kuuhaku.robot.utils;

import org.apache.commons.lang3.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author by kuuhaku
 * @Date 2021/2/16 3:24
 * @Description 文字转图片工具，防屏蔽
 */
public class MojiUtil {

    /**
     * 注意路径的文件格式jpg
     *
     * @param moji 待转换字符串
     * @param path 文件路径
     * @return 文件路径
     */
    public static boolean createImage(String moji, String path) {
        String[] strArr = moji.split("\n");
        // 每张图片的高度，请确保能容纳下所有文字，不然会丢失第一页内容
        int imageHeight = 800;
        // 每行或者每个文字的高度
        int lineHeight = 30;
        // 每张图片有多少行文字
        int everyLine = imageHeight / lineHeight;
        try {
            // 注意字体选对中文支持的字体
            createImage(strArr, path, new Font("宋体", Font.PLAIN, 22), 550, imageHeight, everyLine, lineHeight);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean createImage(String moji, String path, int height, int width) {
        String[] strArr = moji.split("\n");
        // 每行或者每个文字的高度
        int lineHeight = 30;
        // 每张图片有多少行文字
        int everyLine = height / lineHeight;
        try {
            // 注意字体选对中文支持的字体
            createImage(strArr, path, new Font("宋体", Font.PLAIN, 22), width, height, everyLine, lineHeight);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

        //FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        //Rectangle rec = font.getStringBounds("a", frc).getBounds();
        //double height = rec.getHeight();
        //double width = rec.getWidth();
        //FontMetrics fm = sun.font.FontDesignMetrics.getMetrics(font);
        // 标点符号也算一个字
        //int stringWidth = fm.charWidth('字');
        // 目前默认是22
        int stringWidth = font.getSize();
        int lineStringNum = width % stringWidth == 0 ? (width / stringWidth) : (width / stringWidth) + 1;

        List<String> newList = new ArrayList<>();
        List<String> listStr = new ArrayList<>(Arrays.asList(strArr));
        for (int j = 0; j < listStr.size(); j++) {
            if (listStr.get(j).length() > lineStringNum) {
                newList.add(listStr.get(j).substring(0, lineStringNum));
                listStr.add(j + 1, listStr.get(j).substring(lineStringNum));
                listStr.set(j, listStr.get(j).substring(0, lineStringNum));
            } else {
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
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, imageHeight);
            // 字体颜色白色
            g.setColor(Color.BLACK);
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

    /**
     * 拆分集合
     *
     * @param <T>           泛型对象
     * @param resList       需要拆分的集合
     * @param subListLength 每个子集合的元素个数
     * @return 返回拆分后的各个集合组成的列表
     * 代码里面用到了guava和common的结合工具类
     **/
    public static <T> List<List<T>> split(List<T> resList, int subListLength) {
        if (ObjectUtils.isEmpty(resList)) {
            return new ArrayList<>();
        }
        if (subListLength <= 0) {
            throw new RuntimeException("subListLength == 0");
        }
        List<List<T>> ret = new ArrayList<>();
        int size = resList.size();
        if (size <= subListLength) {
            // 数据量不足 subListLength 指定的大小
            ret.add(resList);
        } else {
            int pre = size / subListLength;
            int last = size % subListLength;
            // 前面pre个集合，每个大小都是 subListLength 个元素
            for (int i = 0; i < pre; i++) {
                List<T> itemList = new ArrayList<>();
                for (int j = 0; j < subListLength; j++) {
                    itemList.add(resList.get(i * subListLength + j));
                }
                ret.add(itemList);
            }
            // last的进行处理
            if (last > 0) {
                List<T> itemList = new ArrayList<>();
                for (int i = 0; i < last; i++) {
                    itemList.add(resList.get(pre * subListLength + i));
                }
                ret.add(itemList);
            }
        }
        return ret;
    }
}


