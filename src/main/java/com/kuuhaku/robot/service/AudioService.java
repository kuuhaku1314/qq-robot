package com.kuuhaku.robot.service;

import com.kuuhaku.robot.core.chain.ChannelContext;
import com.kuuhaku.robot.core.service.DownloadService;
import com.kuuhaku.robot.core.service.VoiceService;
import com.kuuhaku.robot.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Audio;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
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
    private MoeGoe moeGoe;


    public static void main(String[] args) throws Exception {
        // 启动你的命令行程序
        ProcessBuilder pb = new ProcessBuilder("D:\\software\\MoeGoe\\MoeGoe.exe");
        Process p = pb.start();

        OutputStream stdin = p.getOutputStream();
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
        System.out.println("Done!");
        MoeGoe moeGoe = new MoeGoe("D:\\software\\MoeGoe\\MoeGoe.exe", "D:\\software\\MoeGoe\\config\\1026_epochs.pth",
                "D:\\software\\MoeGoe\\config\\config-1026.json",
                "D:\\software\\MoeGoe\\config\\model.onnx", "D:\\software\\MoeGoe\\config\\laffey.wav");
        moeGoe.generate("D:\\temp\\h333.wav", "你在说什么呢", true);
        moeGoe.generate("D:\\temp\\h444.wav", "你在说什么呢", true);
        moeGoe.generate("D:\\temp\\h555.wav", "你在说什么呢", true);
        moeGoe.destroy();
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


    // 不再每次启动都程序跑一个程序读取模型拖慢性能
    public MessageChain ReadTextV2(ChannelContext ctx, String text, boolean useChinese) {
        // 注意要读的文本不能换行否则会当成多个指令
        text = text.replace("\r\n", " ").replace("\n", " ");
        if (text.length() == 0) {
            return null;
        }
        String tempPath = downloadService.getRandomPath();
        lock.lock();
        if (moeGoe == null) {
            moeGoe = new MoeGoe(moeGoePath, modelPath, modelConfigPath, emotionalModelConfigPath, emotionalWavPath);
        }
        try {
            moeGoe.generate(tempPath, text, useChinese);
        } catch (Exception e) {
            e.printStackTrace();
            moeGoe.destroy();
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


class MoeGoe {
    public final String moeGoePath;
    public final String modelPath;
    public final String modelConfigPath;
    public final String emotionalModelConfigPath;
    public final String emotionalWavPath;
    private boolean initialized;
    private Process p;
    private BufferedWriter writer;
    private BufferedReader reader;
    private boolean isDestroyed;
    private final char[] buffer = new char[10240];

    public MoeGoe(String moeGoePath, String modelPath, String modelConfigPath, String emotionalModelConfigPath, String emotionalWavPath) {
        this.moeGoePath = moeGoePath;
        this.modelPath = modelPath;
        this.modelConfigPath = modelConfigPath;
        this.emotionalModelConfigPath = emotionalModelConfigPath;
        this.emotionalWavPath = emotionalWavPath;
        initialized = false;
    }

    // 非线程安全
    public void generate(String path, String text, boolean useChinese) throws Exception {
        if (isDestroyed) {
            throw new RuntimeException("MoeGoe is destroyed");
        }
        String[] commands;
        if (!initialized) {
            ProcessBuilder pb = new ProcessBuilder(moeGoePath);
            p = pb.start();
            writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), "GBK"));
            reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "GBK"));
            initialized = true;
            if (useChinese) {
                commands = new String[]{
                        modelPath + "\n",
                        modelConfigPath + "\n",
                        emotionalModelConfigPath + "\n",
                        "t\n",
                        "[ZH]" + text + "[ZH]" + "\n",
                        "1\n",
                        emotionalWavPath + "\n",
                        path + "\n",
                        "y\n"
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
                        path + "\n",
                        "y\n"
                };
            }
        } else {
            if (useChinese) {
                commands = new String[]{
                        "t\n",
                        "[ZH]" + text + "[ZH]" + "\n",
                        "1\n",
                        emotionalWavPath + "\n",
                        path + "\n",
                        "y\n"
                };
            } else {
                commands = new String[]{
                        "t\n",
                        "[JA]" + text + "[JA]" + "\n",
                        "1\n",
                        emotionalWavPath + "\n",
                        path + "\n",
                        "y\n"
                };
            }
        }
        for (String command : commands) {
            writer.write(command);
        }
        writer.flush();
        StringBuilder result = new StringBuilder();
        // 阻塞等待执行完毕
        long now = System.currentTimeMillis();
        long deadline = now + 1000*120;
        while (true) {
            if (reader.ready()) {
                int length = reader.read(buffer);
                if (length == 0) {
                    ThreadUtil.sleep(1000);
                    now += 1000;
                } else {
                    result.append(Arrays.copyOfRange(buffer, 0, length));
                    String curStr = result.toString();
                    if (curStr.contains("Continue? (y/n):")) {
                        System.out.println(curStr);
                        break;
                    }
                }
            } else {
                ThreadUtil.sleep(1000);
                now += 1000;
            }
            if (now > deadline) {
                throw new RuntimeException("generate voice timeout");
            }
        }

        // 可能还需要等待y执行完毕
        ThreadUtil.sleep(100);
    }

    public void destroy() {
        if (isDestroyed) {
            return;
        }
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (p != null) {
                p.destroyForcibly();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isDestroyed = true;
        System.out.println("destroy success");
    }

}