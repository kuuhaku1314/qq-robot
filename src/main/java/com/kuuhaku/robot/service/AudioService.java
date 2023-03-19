package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.VoiceService;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Audio;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class AudioService {

    private final ReentrantLock lock = new ReentrantLock();
    @Value("${robot.moegoe.path}")
    public String moeGoePath;
    @Value("${robot.moegoe.model.path}")
    public String modelPath;
    @Value("${robot.moegoe.model.config}")
    public String modelConfigPath;
    @Value("${robot.moegoe.emotional.model.path}")
    public String emotionalModelConfigPath;
    @Value("${robot.moegoe.emotional.model.wav}")
    public String emotionalWavPath;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private VoiceService voiceService;

    public static void main(String[] args) throws Exception {
        // 启动你的命令行程序
        ProcessBuilder pb = new ProcessBuilder("D:\\software\\MoeGoe\\MoeGoe.exe");
        Process p = pb.start();

        OutputStream stdin = p.getOutputStream();
        InputStream errorStream = p.getErrorStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin, "GBK"));

        // 将多条命令写入标准输入流
        String[] commands = {
                "D:\\software\\MoeGoe\\config\\1026_epochs.pth\n",
                "D:\\software\\MoeGoe\\config\\config-1026.json\n",
                "D:\\software\\MoeGoe\\config\\model.onnx\n",
                "t\n",
                "[ZH]你在说什么呢[ZH]\n",
                "0\n",
                "D:\\software\\MoeGoe\\config\\laffey.wav\n",
                "D:\\temp\\h.wav\n",
                "n\n"
        };
        for (String command : commands) {
            writer.write(command);
        }
        writer.flush();

        // 等待命令行程序执行完毕
        int exitCode = p.waitFor();

        // 获取标准输出流，并指定字符编码
        InputStream stdout = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, "GBK"));

        // 读取命令行程序的输出
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader = new BufferedReader(new InputStreamReader(p.getErrorStream(), "GBK"));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("Done!");
    }

    public MessageChain ReadText(ChannelContext ctx, String text, boolean useChinese) {
        text = text.replace("\n", " ").replace("\r\n", " ");
        if (text.length() == 0) {
            return null;
        }
        lock.lock();
        String tempPath = downloadService.getRandomPath();
        try {
            // 启动你的命令行程序
            ProcessBuilder pb = new ProcessBuilder(moeGoePath);
            Process p = pb.start();

            OutputStream stdin = p.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin, "GBK"));

            // 将多条命令写入标准输入流
            String[] commands;
            if (useChinese) {
                commands = new String[]{
                        modelPath + "\n",
                        modelConfigPath + "\n",
                        emotionalModelConfigPath + "\n",
                        "t\n",
                        "[ZH]" + text + "[ZH]" + "\n",
                        "1\n",
                        emotionalWavPath + "\n",
                        tempPath + "\n",
                        "n\n"
                };
            } else {
                commands = new String[]{
                        modelPath + "\n",
                        modelConfigPath + "\n",
                        emotionalModelConfigPath + "\n",
                        "t\n",
                        "[JA]" + text + "[JA]" + "\n",
                        "1\n",
                        emotionalWavPath + "\n",
                        tempPath + "\n",
                        "n\n"
                };
            }
            for (String command : commands) {
                writer.write(command);
            }
            writer.flush();

            // 等待命令行程序执行完毕
            int exitCode = p.waitFor();


            // 获取标准输出流，并指定字符编码
            InputStream stdout = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, "GBK"));

            // 读取命令行程序的输出
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            if (exitCode != 0) {
                return MessageUtils.newChain().plus("生成失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageUtils.newChain().plus("生成失败");
        } finally {
            lock.unlock();
        }
        Audio audio = voiceService.uploadAudio(tempPath, (Group) ctx.group());
        // downloadService.deleteFile(tempPath);
        log.info("success, path={{}}", tempPath);

        return voiceService.parseMsgChainByAudio(audio);
    }
}