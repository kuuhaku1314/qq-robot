package com.kuuhaku.robot.service.searchApi;

import com.kuuhaku.robot.utils.HttpsUtil;

/**
 * @Author   by kuuhaku
 * @Date     2021/2/14 23:15
 * @Description 识图
 */
@Deprecated
public class AsciiApi {
    /**
     * 二次元图像检索Url
     */
    public final String url = "https://ascii2d.net/search/uri/";

    public void search(String imageUrl) throws Exception{
        String uri = url + imageUrl;
        byte[] bytes = HttpsUtil.doGet(uri);
        String htmlText = new String(bytes);
    }


    public static void main(String[] args) {
        try {
            new AsciiApi().search("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
