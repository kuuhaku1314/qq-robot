package com.kuuhaku.robot.biliClient.api.user;

import com.kuuhaku.robot.biliClient.BiliRequestFactor;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.model.user.User;

public class UserConditionImpl implements IUserCondition {
    public Gettable<User> withUID(Long uid) {
        return new UserGet(BiliRequestFactor.getBiliRequest().setPath("/x/space/acc/info")
                .setParams("mid", String.valueOf(uid)));
    }

    public Gettable<User> withMe() {
        return new UserGet(BiliRequestFactor.getBiliRequest().setPath("/x/space/myinfo"));
    }
}
