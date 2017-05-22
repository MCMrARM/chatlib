package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public abstract class UserInfoApi {

    private List<UserNickChangeListener> listeners = new ArrayList<>();

    public void addNickChangeListener(UserNickChangeListener listener) {
        listeners.add(listener);
    }

    public void removeNickChangeListener(UserNickChangeListener listener) {
        listeners.remove(listener);
    }

    abstract public Future<UserInfo> getUser(UUID uuid, ResponseCallback<UserInfo> callback,
                                             ResponseErrorCallback errorCallback);

    abstract public Future<UserInfo> getUser(String nick, String user, String host,
                                             ResponseCallback<UserInfo> callback, ResponseErrorCallback errorCallback);

    public void notifyNickChange(UserInfo userInfo, String newNick) {
        String oldNick = userInfo.getCurrentNick();
        userInfo.setCurrentNick(newNick);
        for (UserNickChangeListener listener : listeners)
            listener.onNickChanged(userInfo, oldNick, newNick);
    }

}
