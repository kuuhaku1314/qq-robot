package com.kuuhaku.robot.service.imageApi;

import com.kuuhaku.robot.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author by kuuhaku
 * @Date 2021/2/13 18:02
 * @Description 图片api工厂
 */
@Component
public class ImageApiFactory {
    @Autowired
    @Qualifier("apiOne")
    private ApiOne apiOne;
    @Autowired
    @Qualifier("apiTwo")
    private ApiTwo apiTwo;
    @Autowired
    @Qualifier("apiThree")
    private ApiThree apiThree;
    @Autowired
    @Qualifier("apiFour")
    private ApiFour apiFour;
    @Autowired
    @Qualifier("apiFive")
    private ApiFive apiFive;
    @Autowired
    @Qualifier("apiSix")
    private ApiSix apiSix;
    private final List<CommonImageApi> list = new ArrayList<>();
    private static int size;

    @PostConstruct
    public void init() {
        list.add(apiOne);
        list.add(apiTwo);
        list.add(apiThree);
        size = list.size();
    }

    public CommonImageApi getRandomApi() {
        return list.get(RandomUtil.random(size));
    }
}
