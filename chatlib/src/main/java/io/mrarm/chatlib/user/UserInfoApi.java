package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;

import java.util.UUID;
import java.util.concurrent.Future;

public interface UserInfoApi {

    Future<UserInfo> getUser(UUID uuid, ResponseCallback<UserInfo> callback, ResponseErrorCallback errorCallback);

    Future<UserInfo> getUser(String nick, String user, String host, ResponseCallback<UserInfo> callback,
                             ResponseErrorCallback errorCallback);

    Future<Void> subscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                      ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                        ResponseErrorCallback errorCallback);

}
