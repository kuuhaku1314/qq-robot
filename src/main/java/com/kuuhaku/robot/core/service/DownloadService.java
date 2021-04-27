package com.kuuhaku.robot.core.service;

import com.kuuhaku.robot.utils.HttpUtil;
import com.kuuhaku.robot.utils.HttpsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/15 2:11
 * @Description 下载服务
 */
@Service
public class DownloadService {
    @Value("${robot.temp.path}")
    private String basePath;

    private final String schema = "https";

    /**
     * 下载文件
     * @param urlStr 文件url
     * @param path 本地路径
     */
    public void download(String urlStr, String path) {
        //类型根据创建连接
        HttpURLConnection conn = null;
        try {
            if (urlStr.startsWith(schema)) {
                conn = HttpsUtil.getHttpsURLConnection(urlStr, HttpUtil.REQUEST_METHOD_GET);
            } else {
                conn = HttpUtil.getHttpURLConnection(urlStr, HttpUtil.REQUEST_METHOD_GET, null);
            }
            InputStream inStream = conn.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            //把信息存下来，写入内存
            byte[] data = outStream.toByteArray();
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分配随机路径，无后缀
     * @return 无后缀随机文件路径
     */
    public String getRandomPath() {
        return basePath + UUID.randomUUID().toString();
    }

    /**
     * 分配随机路径
     * @return 带png后缀随机文件路径
     */
    public String getRandomPngPath() {
        return getRandomPath() + ".png";
    }

    /**
     * 删除指定文件
     * @param path 文件路径
     */
    public void deleteFile(String path) {
        new File(path).delete();
    }

    /**
     * 获取目录下图片
     * @param path 文件路径
     * @return 文件列表
     */
    public static ArrayList<String> getImageFiles(String path) {
        ArrayList<String> files = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png") ||
                name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".PNG"));
        if (tempList == null) {
            return files;
        }
        for (File f : tempList) {
            if (f.isFile()) {
                files.add(f.toString());
            }
        }
        return files;
    }

    /**
     * 扫描文件夹，获取目录下文件夹
     * @param path 文件路径
     * @return 文件夹列表
     */
    public static ArrayList<String> getFolder(String path) {
        ArrayList<String> files = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles((dir, name) -> dir.isDirectory());
        if (tempList == null) {
            return files;
        }
        for (File f : tempList) {
            files.add(f.toString());
        }
        return files;
    }

    /**
     * 递归版，获取目录下所有文件
     * @param path 路径
     */
    public static List<String> getFiles(String path) {
        ArrayList<String> list = new ArrayList<>();
        getFiles(list, path);
        return list;
    }

    private static void getFiles(List<String> files, String path) {
        File file = new File(path);
        File[] tempList = file.listFiles();
        if (tempList == null) {
            return;
        }
        for (File value : tempList) {
            if (value.isFile()) {
                files.add(value.toString());
            } else {
                getFiles(files, value.getAbsolutePath());
            }
        }
    }

    /**
     * 获取目录下amr格式文件
     * @param path 路径
     * @return 文件路径
     */
    public static ArrayList<String> getAmrFiles(String path) {
        ArrayList<String> files = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles((dir, name) -> name.endsWith(".amr"));
        if (tempList == null) {
            return files;
        }
        for (File f : tempList) {
            if (f.isFile()) {
                files.add(f.toString());
            }
        }
        return files;
    }

    public static void main(String[] args) {
        getImageFiles(getFolder("D:\\image\\eh").get(0)).forEach(System.out::println);
    }
}
