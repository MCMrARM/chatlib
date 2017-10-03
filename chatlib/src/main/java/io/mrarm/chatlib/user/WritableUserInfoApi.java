package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;

import java.util.UUID;
import java.util.concurrent.Future;

public interface WritableUserInfoApi extends UserInfoApi {

    Future<Void> setUserNick(UUID user, String newNick, ResponseCallback<Void> callback,
                             ResponseErrorCallback errorCallback);

    Future<Void> setUserChannelPresence(UUID user, String channel, boolean present, ResponseCallback<Void> callback,
                                        ResponseErrorCallback errorCallback);

    Future<Void> clearAllUsersChannelPresences(ResponseCallback<Void> callback, ResponseErrorCallback errorCallback);

}
