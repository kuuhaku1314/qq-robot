package com.kuuhaku.robot.service.musicApi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.entity.music.NetEaseMusic;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.MusicKind;
import net.mamoe.mirai.message.data.MusicShare;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 21:42
 * @Description 网易云音乐Api
 */
@Service
@Slf4j
public class NetEaseMusicApi {
    @Value("${robot.music.cookie}")
    private String cookie;

    public List<NetEaseMusic> getNetEaseMusicPage(String musicName) {
        String jsonStr = null;
        List<NetEaseMusic> list = new ArrayList<>();
        try {
            // 转换成encode
            String str = URLEncoder.encode(musicName, StandardCharsets.UTF_8);
            //拼接url
            URL url = new URL("http://music.163.com/api/search/pc?type=1&s=" + str + "&type=1&limit=10");
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setConnectTimeout(3000);
            httpCon.setDoInput(true);
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("Cookie", cookie);
            // 获取相应码
            int respCode = httpCon.getResponseCode();
            if (respCode == HttpStatus.OK.value()) {
                // ByteArrayOutputStream相当于内存输出流
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                // 将输入流转移到内存输出流中
                while ((len = httpCon.getInputStream().read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, len);
                }
                // 将内存流转换为字符串
                jsonStr = out.toString();
                log.info(jsonStr);
                JSONArray jsonArray = JSONArray.parseArray(JSONObject.parseObject(JSONObject.parseObject(jsonStr).getString("result")).getString("songs"));
                //jsonArray 内就是歌曲信息了 之后可以用jsonArray.getJSONObject(0).getString(/*这里放你要获取的字段名如id*/)获取详细信息
                if (jsonArray == null) {
                    log.info("音乐信息json为空");
                    return null;
                }
                for (int i = 0; i < jsonArray.size(); i++) {
                    NetEaseMusic netEaseMusic = new NetEaseMusic();
                    netEaseMusic.setId(jsonArray.getJSONObject(i).getString("id"));
                    netEaseMusic.setName(jsonArray.getJSONObject(i).getString("name"));
                    JSONArray artists = JSONArray.parseArray(JSONObject.parseObject(jsonArray.getString(i)).getString("artists"));
                    List<String> artistList = new ArrayList<>();
                    for (int j = 0; j < artists.size(); j++) {
                        artistList.add(JSONObject.parseObject(artists.getString(j)).getString("name"));
                    }
                    netEaseMusic.setArtists(artistList);
                    netEaseMusic.setPicUrl(JSONObject.parseObject(jsonArray.getJSONObject(i).getString("album")).getString("picUrl"));
                    list.add(netEaseMusic);
                }
                return list;
            } else {
                log.info("云音乐解析出错");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MusicShare getMusicCard(NetEaseMusic netEaseMusic) {
        String musicUrl = "http://music.163.com/song/media/outer/url?id=" + netEaseMusic.getId() + ".mp3";
        String picUrl = netEaseMusic.getPicUrl();
        String jumpUrl = "https://music.163.com/#/song?id=" + netEaseMusic.getId();
        String title = netEaseMusic.getName();
        String summary;
        List<String> artists = netEaseMusic.getArtists();
        summary = String.join("/", artists);
        return new MusicShare(MusicKind.NeteaseCloudMusic, title, summary, jumpUrl, picUrl, musicUrl);
    }

    public String getMusicUrl(NetEaseMusic netEaseMusic) {
        return "http://music.163.com/song/media/outer/url?id=" + netEaseMusic.getId() + ".mp3";
    }

    public static void main(String[] args) {
        new NetEaseMusicApi().getNetEaseMusicPage("雪恋少女");
    }
}
