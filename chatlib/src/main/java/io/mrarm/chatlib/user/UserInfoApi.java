package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

public interface UserInfoApi {

    Future<List<String>> findUsers(String query, ResponseCallback<List<String>> callback,
                                   ResponseErrorCallback errorCallback);

    Future<UserInfo> getUser(UUID uuid, ResponseCallback<UserInfo> callback, ResponseErrorCallback errorCallback);

    Future<UserInfo> getUser(String nick, String user, String host, ResponseCallback<UserInfo> callback,
                             ResponseErrorCallback errorCallback);

    Future<UUID> resolveUser(String nick, String user, String host, ResponseCallback<UUID> callback,
                             ResponseErrorCallback errorCallback);

    Future<Map<String, UUID>> resolveUsers(List<String> nicks, ResponseCallback<Map<String, UUID>> callback,
                                           ResponseErrorCallback errorCallback);

    Future<Map<UUID, String>> getUsersNicks(List<UUID> uuids, ResponseCallback<Map<UUID, String>> callback,
                                            ResponseErrorCallback errorCallback);

    Future<Void> subscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                      ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                        ResponseErrorCallback errorCallback);

}
