package com.kuuhaku.robot.biliClient.model.dynamic;

import com.kuuhaku.robot.biliClient.model.BaseModel;
import com.kuuhaku.robot.biliClient.utils.TransDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@Getter
@Setter
public class DynamicBase implements BaseModel {
    /**
     * type 1 转发
     * type 2 图片动态
     * type 4 文字动态
     * type 8 视频动态投稿
     * type 16 小视频
     * type 64 专栏
     * type 256 音频
     * type 4200 直播
     * type 4201 直播
     * type 4308 直播
     */
    private Integer type;

    private Long uid;

    private Long dynamic_id;

    private Long orig_dy_id;

    private Long timestamp;

    private Long view;

    private Long repost;

    private Long comment;

    private Long like;

    private Integer is_liked;

    // 投稿下有这个字段
    private String bvid;

    public Date createTime() {
        return TransDate.timestampToDate(this.timestamp);
    }
}
