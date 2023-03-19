package com.kuuhaku.robot.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class HttpUtil {
    //请求方式
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";
    //链接超时时间
    private static final int CONNECT_TIME_OUT = 10000;
    //读取超时时间
    private static final int READ_TIME_OUT = 20000;
    //编码格式，UTF-8
    private static final String CHARSET_UTF8 = "UTF-8";

    /**
     * get请求
     *
     * @param connUrl 完整的请求链接
     * @return 接口返回报文
     * @throws IOException 请求异常
     */
    public static String get(String connUrl) throws IOException {
        return get(connUrl, null);
    }

    public static String get(String connUrl, Proxy proxy) throws IOException {
        return get(connUrl, null, proxy);
    }

    public static String get(String connUrl, Map<String, String> header, Proxy proxy) throws IOException {
        HttpURLConnection httpURLConnection = getHttpURLConnection(connUrl, REQUEST_METHOD_GET, proxy);
        //模拟chrome
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
        if (null != header) {
            //加入请求头
            for (String key : header.keySet()) {
                //忽略空的参数
                if (StringUtils.isEmpty(key) || StringUtils.isEmpty(header.get(key))) {
                    continue;
                }
                httpURLConnection.setRequestProperty(key, header.get(key));
            }
        }
        //开始链接
        httpURLConnection.connect();

        //获取错误流
//        httpURLConnection.getErrorStream();
        //获取响应流
        InputStream rspInputStream = httpURLConnection.getInputStream();
        String rspStr = parseInputStreamStr(rspInputStream);

        //关闭流
        rspInputStream.close();
        //断开连接
        httpURLConnection.disconnect();
        return rspStr;
    }

    //转化流
    private static String parseInputStreamStr(InputStream inputStream) throws IOException {
        //编码这里暂时固定utf-8，以后根据返回的编码来
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String tempStr = null;
        //逐行读取
        while ((tempStr = reader.readLine()) != null) {
            stringBuilder.append(tempStr);
            stringBuilder.append("\r\n");
        }
        return stringBuilder.toString();
    }

    public static HttpURLConnection getHttpURLConnection(String connUrl, String method, Proxy proxy) throws IOException {
        //使用url对象打开一个链接
        HttpURLConnection httpURLConnection = null;
        if (null != proxy) {
            httpURLConnection = (HttpURLConnection) new URL(connUrl).openConnection(proxy);
        } else {
            httpURLConnection = (HttpURLConnection) new URL(connUrl).openConnection();
        }
        httpURLConnection.setRequestMethod(method);
        //设置链接超时时间
        httpURLConnection.setConnectTimeout(CONNECT_TIME_OUT);
        //设置返回超时时间
        httpURLConnection.setReadTimeout(READ_TIME_OUT);

        return httpURLConnection;
    }

    /**
     * 转化为urlencode
     * 采用UTF-8格式
     *
     * @param params 转化的参数
     * @return 转化后的urlencode
     * @throws IOException 转化异常
     */
    public static String parseUrlEncode(Map<String, Object> params) throws IOException {
        return parseUrlEncode(params, CHARSET_UTF8);
    }

    /**
     * 转化为urlencode
     *
     * @param params  转化的参数
     * @param charset 编码格式
     * @return 转化后的urlencode
     * @throws IOException 转化异常
     */
    public static String parseUrlEncode(Map<String, Object> params, String charset) throws IOException {
        //非空判断
        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder urlEncode = new StringBuilder();
        Set<Map.Entry<String, Object>> entries = params.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            String name = entry.getKey();
            String value = null;
            if (null != entry.getValue()) {
                value = entry.getValue().toString();
            }
            // 忽略参数名或参数值为空的参数
            if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
                continue;
            }

            //多个参数之间使用&连接
            urlEncode.append("&");
            //转化格式，并拼接参数和值
            urlEncode.append(name).append("=").append(URLEncoder.encode(value, charset));
        }
        if (urlEncode.length() <= 0) {
            return "";
        }
        return "?" + urlEncode.substring(1);
    }

    /**
     * 检查端口是否可用
     * 经过测试，好像只要端口被占用了就判定链接成功
     * ssr不开局域网端口，或者开了以后没开代理，端口也是通的
     *
     * @param address 地址
     * @param prot    端口
     * @return 是否可用
     */
    public static boolean checkPort(String address, int prot) {
        if (StringUtils.isEmpty(address)) {
            return false;
        }
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(address, prot));
            socket.close();
            return true;
        } catch (IOException ioEx) {
            //没那么重要，直接打在控制台里
            ioEx.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException ioEx) {
            //没那么重要，直接打在控制台里
            ioEx.printStackTrace();
        }

        return false;
    }

    /**
     * 获取代理信息
     */
    public static Proxy getProxy() {
        // 创建代理 地址和端口写为配置
        Proxy proxy = null;
        if (HttpUtil.checkPort("127.0.0.1", 31051)) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 31051));
        }
        return proxy;
    }


    public static boolean SendImage(String src, String des) throws IOException {
        String urlToConnect = "http://127.0.0.1:7860/api/predict/";

        HttpURLConnection connection = (HttpURLConnection) new URL(urlToConnect).openConnection();
        connection.setDoOutput(true); // This sets request method to POST.
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Connection", "keep-alive");
        ImageReq req = new ImageReq();
        List<String> al = new ArrayList<>();
        String header = "data:image/png;base64,";
        String content = imageToBase64Str(src);
        content = header + content;
        al.add(content);
        req.data = al;
        try (OutputStream os = connection.getOutputStream()) {
            os.write(JSONObject.toJSONString(req).getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpStatus.OK.value()) {
            return false;
        }

        StringBuilder strBuf = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            strBuf.append(line).append("\n");
        }
        String json = strBuf.toString();
        reader.close();
        connection.disconnect();
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        if (jsonArray.isEmpty()) {
            return false;
        }
        String value = jsonArray.get(0).toString();
        String img = StringUtils.removeFirst(value, "data:image/png;base64,");
        return base64StrToImage(img, des);
    }

    public static boolean base64StrToImage(String imgStr, String path) {
        if (imgStr == null)
            return false;
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            // 解密
            byte[] b = decoder.decode(imgStr);
            // 处理数据
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //文件夹不存在则自动创建
            File tempFile = new File(path);
            OutputStream out = new FileOutputStream(tempFile);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 图片转base64字符串
     *
     * @param imgFile 图片路径
     * @return
     */
    public static String imageToBase64Str(String imgFile) {
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 加密
        Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(data));
    }

    public static String getColorImage(String src, String faceID, String r, String g, String b, String h) throws IOException {
        String urlToConnect = "http://127.0.0.1:8233/upload_sketch";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(urlToConnect);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("room", "new"));
        String header = "data:image/png;base64,";
        String content = imageToBase64Str(src);
        params.add(new BasicNameValuePair("sketch", header + content));
        httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        CloseableHttpResponse response = client.execute(httpPost);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println(responseCode);
        if (responseCode != HttpStatus.OK.value()) {
            return "";
        }
        String str = EntityUtils.toString(response.getEntity(), "utf8");
        System.out.println(str);
        String[] s1 = StringUtils.split(str, "_");
        if (s1.length == 0) {
            return "";
        }

        httpPost = new HttpPost("http://127.0.0.1:8233/request_result");
        params = new ArrayList<>();
        params.add(new BasicNameValuePair("room", s1[0]));
        if (faceID != null) {
            params.add(new BasicNameValuePair("faceID", faceID));
        } else {
            params.add(new BasicNameValuePair("faceID", "65552"));
        }
        params.add(new BasicNameValuePair("inv4", "1"));
        params.add(new BasicNameValuePair("need_render", "0"));
        params.add(new BasicNameValuePair("skipper", "null"));
        params.add(new BasicNameValuePair("face", "0"));
        params.add(new BasicNameValuePair("points", "[]"));

        if (r != null) {
            params.add(new BasicNameValuePair("r", r));
        } else {
            params.add(new BasicNameValuePair("r", "0.99"));
        }
        if (g != null) {
            params.add(new BasicNameValuePair("g", g));
        } else {
            params.add(new BasicNameValuePair("g", "0.83"));
        }
        if (b != null) {
            params.add(new BasicNameValuePair("b", b));
        } else {
            params.add(new BasicNameValuePair("b", "0.66"));
        }
        if (h != null) {
            params.add(new BasicNameValuePair("h", h));
        } else {
            params.add(new BasicNameValuePair("h", "0.16"));
        }

        params.add(new BasicNameValuePair("d", "0"));
        httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        response = client.execute(httpPost);
        responseCode = response.getStatusLine().getStatusCode();
        System.out.println(responseCode);
        if (responseCode != HttpStatus.OK.value()) {
            return "";
        }
        str = EntityUtils.toString(response.getEntity(), "utf8");
        System.out.println(str);
        s1 = StringUtils.split(str, "_");
        if (s1.length != 2) {
            return "";
        }
        return "http://127.0.0.1:8233/rooms/" + s1[0] + "/" + s1[1] + ".blended_smoothed_careful.png";
    }

    public static String encodeURL(String url) {
        return URLEncoder.encode(url);
    }


    public static void main(String[] args) {
        try {
            System.out.println(getColorImage("C:\\Users\\admin\\Desktop\\WWW.png", null, null, null, null, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class ImageReq {
    public List<String> data;
}