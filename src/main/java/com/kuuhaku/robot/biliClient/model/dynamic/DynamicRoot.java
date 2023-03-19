package com.kuuhaku.robot.biliClient.model.dynamic;

import com.alibaba.fastjson.JSONObject;
import com.kuuhaku.robot.biliClient.BiliResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ToString
@Getter
@Setter
public class DynamicRoot {
    private static final Logger log = LoggerFactory.getLogger(DynamicRoot.class);

    private Integer hasMore;

    private List<Dynamic> items = new ArrayList<>();

    private Long nextOffset = 0L;

    public static DynamicRoot build(BiliResult result) {
        try {
            DynamicRoot dynamicRoot = result.toData(DynamicRoot.class);
            if (dynamicRoot.hasMore != 1)
                return dynamicRoot;
            List<Dynamic> cards = JSONObject.parseObject(result.getData().toString()).getJSONArray("cards").toJavaList(DynamicRaw.class).stream().map(DynamicRaw::toDynamic).filter(Objects::nonNull).collect(Collectors.toList());
            dynamicRoot.setItems(cards);
            return dynamicRoot;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("当前请求解析异常!原始请求数据为\n {}", result);
            return null;
        }
    }
}
