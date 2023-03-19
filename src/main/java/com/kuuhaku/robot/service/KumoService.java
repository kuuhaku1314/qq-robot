package com.kuuhaku.robot.service;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.filter.Filter;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import com.kennycason.kumo.palette.LinearGradientColorPalette;
import com.kuuhaku.robot.core.service.DownloadService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author by kuuhaku
 * @Date 2021/6/1 14:11
 * @Description
 */
@Service
public class KumoService {

    @Autowired
    private DownloadService downloadService;

    public static void main(String[] args) {
        KumoService kumoService = new KumoService();
        kumoService.parseLog();
    }

    public String getKumoPath(String id) {
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setWordFrequenciesToReturn(150);
        frequencyAnalyzer.setMinWordLength(2);
        frequencyAnalyzer.addFilter(new Filter() {
            @Override
            public boolean test(String s) {
                return !s.startsWith("@") && !StringUtils.isNumeric(s);
            }
        });

        //引入中文解析器
        frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
        //指定文本文件路径，生成词频集合
        final List<WordFrequency> wordFrequencyList;
        try {
            wordFrequencyList = frequencyAnalyzer.load("D:\\temp\\fenxi\\" + id + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //设置图片分辨率
        Dimension dimension = new Dimension(1200, 1200);
        //此处的设置采用内置常量即可，生成词云对象
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        //设置边界及字体
        wordCloud.setPadding(2);
        java.awt.Font font = new java.awt.Font("STSong-Light", Font.ITALIC, 20);
        //设置词云显示的三种颜色，越靠前设置表示词频越高的词语的颜色
        wordCloud.setColorPalette(new LinearGradientColorPalette(Color.RED, Color.BLUE, Color.GREEN, 30, 30));
        wordCloud.setKumoFont(new KumoFont(font));
        //设置背景色
        wordCloud.setBackgroundColor(new Color(255, 255, 255));
        //设置背景图片
        //wordCloud.setBackground(new PixelBoundryBackground("E:\\爬虫/google.jpg"));
        //设置背景图层为圆形
        wordCloud.setBackground(new CircleBackground(510));
        wordCloud.setFontScalar(new SqrtFontScalar(12, 45));
        //生成词云
        wordCloud.build(wordFrequencyList);
        String randomPngPath = downloadService.getRandomPngPath();
        //String randomPngPath = "D:\\temp\\xxx.png";
        wordCloud.writeToFile(randomPngPath);
        return randomPngPath;
    }

    public void parseLog() {
        try {
            HashMap<String, List<String>> map = new HashMap<>();
            ArrayList<String> folder = DownloadService.getFolder("D:\\temp\\log");
            folder.forEach(path -> {
                Stream<String> lines = null;
                try {
                    lines = Files.lines(Paths.get(path));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                lines.filter(line -> line.contains("LogHandler")).forEach(line -> {
                    int i1 = line.indexOf("userId:");
                    if (i1 < 0) {
                        return;
                    }
                    int i2 = line.indexOf(",", i1);
                    if (i2 < 0) {
                        return;
                    }
                    String id = line.substring(i1 + 7, i2);
                    int i3 = line.indexOf("msg:");
                    if (i3 < 0) {
                        return;
                    }
                    String msg;
                    int i4 = line.lastIndexOf("]");
                    if (i4 < 0 || i4 < i3) {
                        msg = line.substring(i3 + 4);
                    } else {
                        msg = line.substring(i4 + 1);
                    }
                    if (StringUtils.isNotBlank(msg)) {
                        List<String> list = map.get(id);
                        if (list != null) {
                            list.add(msg);
                        } else {
                            ArrayList<String> list2 = new ArrayList<>();
                            list2.add(msg);
                            map.put(id, list2);
                        }
                    }
                });
            });

            for (Map.Entry<String, List<String>> stringListEntry : map.entrySet()) {
                PrintWriter printWriter = new PrintWriter("D:\\temp\\fenxi\\" + stringListEntry.getKey() + ".txt");
                List<String> value = stringListEntry.getValue();
                value.forEach(printWriter::println);
                printWriter.close();
            }
            //lists.forEach(printWriter::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
