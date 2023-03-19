package com.kuuhaku.robot.biliClient.api.user;

import com.kuuhaku.robot.biliClient.BiliCall;
import com.kuuhaku.robot.biliClient.BiliRequest;
import com.kuuhaku.robot.biliClient.able.Gettable;
import com.kuuhaku.robot.biliClient.exception.BiliRequestException;
import com.kuuhaku.robot.biliClient.model.user.User;

public class UserGet implements Gettable<User> {
    private final BiliRequest request;

    public UserGet(BiliRequest request) {
        this.request = request;
    }

    public User get() {
        try {
            return BiliCall.doCall(this.request).toData(User.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BiliRequestException(this.request.getURI());
        }
    }
}
