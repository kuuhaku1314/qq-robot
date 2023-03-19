package com.kuuhaku.robot.biliClient.model.relation;

import com.kuuhaku.robot.biliClient.model.BaseModel;
import com.kuuhaku.robot.biliClient.model.user.User;
import com.kuuhaku.robot.biliClient.utils.TransDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@Getter
@Setter
public class Follower extends User implements BaseModel {
    private long mtime;

    private String uname;

    public Date createTime() {
        return TransDate.timestampToDate(this.mtime);
    }
}
