package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;

import java.util.concurrent.Future;

public interface WritableUserInfoApi extends UserInfoApi {

    Future<Void> setUserNick(UserInfo userInfo, String newNick, ResponseCallback<Void> callback,
                             ResponseErrorCallback errorCallback);

}
