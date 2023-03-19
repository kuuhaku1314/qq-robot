package com.kuuhaku.robot.biliClient.model.dynamic;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString
@Getter
@Setter
// 生数据，未加工完成
public class DynamicRaw {
    private static final Logger log = LoggerFactory.getLogger(DynamicRaw.class);

    private DynamicBase desc;

    private String card;

    private DynamicDisplay display;

    //   * type 1 转发
    //   * type 2 图片动态
    //   * type 4 文字动态
    //   * type 8 视频动态投稿
    //   * type 1024 直播结束
    //   * type 2048 活动
    //   * type 4020 直播
    //   * type 4021 直播
    //   * type 4308 直播
    public Dynamic toDynamic() {
        if (this.desc == null || this.card == null || this.card.isEmpty())
            return null;
        try {
            Dynamic dynamic = new Dynamic();
            dynamic.setBase(getDesc());
            dynamic.setDisplay(getDisplay());
            JSONObject dynamicCard = JSONObject.parseObject(getCard());
            switch (desc.getType()) {
                case 1:
                    dynamic.setType(Dynamic.DType.REPOST);
                    DynamicRepost dynamicRepost = new DynamicRepost();
                    dynamic.setRepost(dynamicRepost);
                    JSONObject jsonObject = JSONObject.parseObject(dynamicCard.toString());
                    JSONObject itemObject = JSONObject.parseObject(jsonObject.getString("item"));
                    dynamicRepost.setContent(itemObject.getString("content"));
                    dynamicRepost.setTimestamp(itemObject.getLong("timestamp"));
                    Integer ori_type = itemObject.getInteger("orig_type");
                    Dynamic d = new Dynamic();
                    dynamicRepost.setDynamic(d);
                    String origin = null;
                    switch (ori_type) {
                        case 1:
                            dynamic.setType(Dynamic.DType.REPOST);
                            dynamicRepost = new DynamicRepost();
                            d.setRepost(dynamicRepost);
                            jsonObject = JSONObject.parseObject(dynamicCard.toString());
                            itemObject = JSONObject.parseObject(jsonObject.getString("item"));
                            dynamicRepost.setContent(itemObject.getString("content"));
                            dynamicRepost.setTimestamp(itemObject.getLong("timestamp"));
                            break;
                        case 2:
                            origin = JSONObject.parseObject(jsonObject.getString("origin")).toString();
                            d.setType(Dynamic.DType.IMAGE);
                            d.setImage((JSONObject.parseObject(JSONObject.parseObject(origin).getString("item"), DynamicImage.class)));
                            break;
                        case 4:
                            origin = JSONObject.parseObject(jsonObject.getString("origin")).toString();
                            d.setType(Dynamic.DType.WORD);
                            d.setWord((JSONObject.parseObject(JSONObject.parseObject(origin).getString("item"), DynamicWord.class)));
                            break;
                        case 8:
                            origin = JSONObject.parseObject(jsonObject.getString("origin")).toString();
                            d.setType(Dynamic.DType.VIDEO);
                            d.setVideo(JSONObject.parseObject(origin, DynamicVideo.class));
                            break;
                        case 2048:
                            origin = JSONObject.parseObject(jsonObject.getString("origin")).toString();
                            d.setType(Dynamic.DType.ACTIVITY);
                            d.setActivity(JSONObject.parseObject(origin, DynamicActivity.class));
                            break;
                        case 4200:
                            origin = JSONObject.parseObject(jsonObject.getString("origin")).toString();
                            d.setType(Dynamic.DType.LIVE);
                            d.setLive(JSONObject.parseObject(origin, DynamicLive.class));
                            break;
                        case 4308:
                            origin = JSONObject.parseObject(jsonObject.getString("origin")).toString();
                            d.setType(Dynamic.DType.LIVE);
                            d.setLive(JSONObject.parseObject(JSONObject.parseObject(origin).getString("live_play_info"), DynamicLive.class));
                            break;
                        default:
                            d.setType(Dynamic.DType.NOT_SUPPORTED);
                            DynamicNotSupported notSupported = new DynamicNotSupported();
                            notSupported.setDesc("unsupported dynamic type = " + ori_type);
                            d.setNotSupported(notSupported);
                    }
                    break;
                case 2:
                    dynamic.setType(Dynamic.DType.IMAGE);
                    dynamic.setImage(JSONObject.parseObject(JSONObject.parseObject(dynamicCard.toString()).getString("item"), DynamicImage.class));
                    break;
                case 4:
                    dynamic.setType(Dynamic.DType.WORD);
                    dynamic.setWord(JSONObject.parseObject(JSONObject.parseObject(dynamicCard.toString()).getString("item"), DynamicWord.class));
                    break;
                case 8:
                    dynamic.setType(Dynamic.DType.VIDEO);
                    dynamic.setVideo(JSONObject.parseObject(dynamicCard.toString(), DynamicVideo.class));
                    break;
                case 2048:
                    dynamic.setType(Dynamic.DType.ACTIVITY);
                    dynamic.setActivity(JSONObject.parseObject(dynamicCard.toString(), DynamicActivity.class));
                    break;
                case 4200:
                    dynamic.setType(Dynamic.DType.LIVE);
                    dynamic.setLive(JSONObject.parseObject(dynamicCard.toString(), DynamicLive.class));
                    break;
                case 4308:
                    dynamic.setType(Dynamic.DType.LIVE);
                    dynamic.setLive(JSONObject.parseObject(JSONObject.parseObject(dynamicCard.toString()).getString("live_play_info"), DynamicLive.class));
                    break;
                default:
                    dynamic.setType(Dynamic.DType.NOT_SUPPORTED);
                    DynamicNotSupported notSupported = new DynamicNotSupported();
                    notSupported.setDesc("unsupported dynamic type = " + desc.getType());
                    dynamic.setNotSupported(notSupported);
            }
            return dynamic;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("动态解析遇到错误！可能遇到了格式不支持的动态或该类型为新型动态,原始内容如下\n {}", this);
            return null;
        }
    }
}
