package com.kuuhaku.robot.biliClient.api.user;

import com.kuuhaku.robot.biliClient.BiliCondition;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.model.user.User;

public interface IUserCondition extends BiliCondition {
    Gettable<User> withUID(Long paramLong);

    Gettable<User> withMe();
}
