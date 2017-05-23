package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class SimpleUserInfoApi implements WritableUserInfoApi {

    protected HashMap<UUID, UserInfo> uuidToUserInfo = new HashMap<>();
    protected HashMap<String, UserInfo> nickToUserInfo = new HashMap<>();

    private List<UserNickChangeListener> nickChangeListeners = new ArrayList<>();

    @Override
    public Future<UserInfo> getUser(UUID uuid, ResponseCallback<UserInfo> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            return new UserInfo(uuidToUserInfo.get(uuid));
        }, callback, errorCallback);
    }

    @Override
    public Future<UserInfo> getUser(String nick, String user, String host, ResponseCallback<UserInfo> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            if (nickToUserInfo.containsKey(nick))
                return new UserInfo(nickToUserInfo.get(nick));
            UserInfo userInfo = new UserInfo(UUID.randomUUID(), nick);
            uuidToUserInfo.put(userInfo.getUUID(), userInfo);
            nickToUserInfo.put(userInfo.getCurrentNick(), userInfo);
            if (callback != null)
                callback.onResponse(userInfo);
            return new UserInfo(userInfo);
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> subscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                             ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            nickChangeListeners.add(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                               ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            nickChangeListeners.remove(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> setUserNick(UserInfo userInfo, String newNick, ResponseCallback<Void> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            String oldNick = userInfo.getCurrentNick();
            userInfo.setCurrentNick(newNick);
            if (nickToUserInfo.get(oldNick) == userInfo) {
                nickToUserInfo.remove(oldNick);
                nickToUserInfo.put(newNick, userInfo);
            }
            for (UserNickChangeListener listener : nickChangeListeners)
                listener.onNickChanged(userInfo, oldNick, newNick);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> setUserChannelPresence(UserInfo userInfo, String channel, boolean present,
                                               ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            userInfo.setChannelPresence(channel, present);
            return null;
        }, callback, errorCallback);
    }
}
