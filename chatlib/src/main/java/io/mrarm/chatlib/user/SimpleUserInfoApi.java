package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.util.InstantFuture;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Future;

public class SimpleUserInfoApi extends UserInfoApi {

    protected HashMap<UUID, UserInfo> uuidToUserInfo = new HashMap<>();
    protected HashMap<String, UserInfo> nickToUserInfo = new HashMap<>();

    @Override
    public Future<UserInfo> getUser(UUID uuid, ResponseCallback<UserInfo> callback,
                                    ResponseErrorCallback errorCallback) {
        UserInfo userInfo = uuidToUserInfo.get(uuid);
        if (callback != null)
            callback.onResponse(userInfo);
        return new InstantFuture<>(userInfo);
    }

    @Override
    public Future<UserInfo> getUser(String nick, String user, String host, ResponseCallback<UserInfo> callback,
                                    ResponseErrorCallback errorCallback) {
        UserInfo userInfo;
        if (nickToUserInfo.containsKey(nick)) {
            userInfo = nickToUserInfo.get(nick);
        } else {
            userInfo = new UserInfo(UUID.randomUUID(), nick);
            uuidToUserInfo.put(userInfo.getUUID(), userInfo);
            nickToUserInfo.put(userInfo.getCurrentNick(), userInfo);
            if (callback != null)
                callback.onResponse(userInfo);
        }
        if (callback != null)
            callback.onResponse(userInfo);
        return new InstantFuture<>(userInfo);
    }

    @Override
    public void notifyNickChange(UserInfo userInfo, String newNick) {
        if (nickToUserInfo.get(userInfo.getCurrentNick()) == userInfo) {
            nickToUserInfo.remove(userInfo.getCurrentNick());
            nickToUserInfo.put(newNick, userInfo);
        }
        super.notifyNickChange(userInfo, newNick);
    }

}
