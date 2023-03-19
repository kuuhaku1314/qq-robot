package com.kuuhaku.robot.biliClient.model.dynamic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author by kuuhaku
 * @date 2022/6/2 2:51
 * @description
 */
@ToString
@Getter
@Setter
public class DynamicActivity {
    private Long rid;
    private Object user;
    private Vest vest;
    private Sketch sketch;

    @ToString
    @Getter
    @Setter
    public class Vest {
        private Long uid;
        private String content;
    }

    @ToString
    @Getter
    @Setter
    public class Sketch {
        private String title;
        private String desc_text;
        private String cover_url;
        private String target_url;
        private Long sketch_id;
        private Long biz_type;
    }
}
