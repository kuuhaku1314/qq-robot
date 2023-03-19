package com.kuuhaku.robot.biliClient;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class BiliResult {
    private Integer code;

    private String message;

    private Object data;

    private String msg;

    public <T> T toData(Class<T> tClass) {
        return JSONObject.parseObject(this.data.toString(), tClass);
    }

    public String toMessage() {
        if (this.message != null && this.message.isEmpty())
            return this.message;
        return this.msg;
    }

    public com.kuuhaku.robot.biliClient.BiliResult check() {
        switch (this.code) {
            case -400:
                System.err.println(this);
                throw new RuntimeException("发起请求异常！" + toMessage());
            case -101:
                System.err.println(this);
                throw new RuntimeException("请求需要认证！" + toMessage());
            case 0:
                return this;
        }
        System.err.println(this);
        throw new RuntimeException("请求错误" + toMessage());
    }
}
