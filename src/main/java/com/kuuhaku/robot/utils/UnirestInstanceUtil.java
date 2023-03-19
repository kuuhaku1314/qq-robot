package com.kuuhaku.robot.utils;

import com.github.markozajc.akiwrapper.core.utils.UnirestUtils;
import kong.unirest.Proxy;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class UnirestInstanceUtil {
    public static void main(String[] args) {
        HookUnirestInstance("127.0.0.1",20081);
    }

    public static void HookUnirestInstance(String host, int port) {
        try {
            UnirestInstance instance = Unirest.spawnInstance();
            instance.config()
                    .addDefaultHeader("Accept",
                            "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*. q=0.01")
                    .addDefaultHeader("Accept-Language", "en-US,en.q=0.9,ar.q=0.8")
                    .addDefaultHeader("X-Requested-With", "XMLHttpRequest")
                    .addDefaultHeader("Sec-Fetch-Dest", "empty")
                    .addDefaultHeader("Sec-Fetch-Mode", "cors")
                    .addDefaultHeader("Sec-Fetch-Site", "same-origin")
                    .addDefaultHeader("Connection", "keep-alive")
                    .addDefaultHeader("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0. Win64. x64) AppleWebKit/537.36" +
                                    "(KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36")
                    .addDefaultHeader("Referer", "https://en.akinator.com/game")
                    .cookieSpec("ignore").proxy(new Proxy(host, port));
            Constructor<UnirestUtils> constructor = UnirestUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            UnirestUtils unirestUtils = constructor.newInstance();
            Field urlField = unirestUtils.getClass().getDeclaredField("singletonUnirest");
            urlField.setAccessible(true);
            urlField.set(urlField, instance);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
