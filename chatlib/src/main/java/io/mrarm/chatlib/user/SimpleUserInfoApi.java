package io.mrarm.chatlib.user;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.util.*;
import java.util.concurrent.Future;

public class SimpleUserInfoApi implements WritableUserInfoApi {

    protected HashMap<UUID, UserInfo> uuidToUserInfo = new HashMap<>();
    protected HashMap<String, UserInfo> nickToUserInfo = new HashMap<>();

    private List<UserNickChangeListener> nickChangeListeners = new ArrayList<>();

    @Override
    public Future<List<String>> findUsers(String query, ResponseCallback<List<String>> callback,
                                          ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                List<String> ret = new ArrayList<>();
                for (UserInfo userInfo : uuidToUserInfo.values()) {
                    String nick = userInfo.getCurrentNick();
                    if (nick.regionMatches(true, 0, query, 0, query.length()))
                        ret.add(nick);
                }
                return ret;
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<UserInfo> getUser(UUID uuid, ResponseCallback<UserInfo> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                return new UserInfo(uuidToUserInfo.get(uuid));
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<UserInfo> getUser(String nick, String user, String host, ResponseCallback<UserInfo> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                UUID uuid = resolveUser(nick, user, host, null, null).get();
                return getUser(uuid, null, null).get();
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<UUID> resolveUser(String nick, String user, String host, ResponseCallback<UUID> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                if (nickToUserInfo.containsKey(nick))
                    return nickToUserInfo.get(nick).getUUID();
                UserInfo userInfo = new UserInfo(UUID.randomUUID(), nick);
                uuidToUserInfo.put(userInfo.getUUID(), userInfo);
                nickToUserInfo.put(userInfo.getCurrentNick(), userInfo);
                return userInfo.getUUID();
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<Map<String, UUID>> resolveUsers(List<String> nicks, ResponseCallback<Map<String, UUID>> callback,
                                                  ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                Map<String, UUID> ret = new HashMap<>();
                for (String nick : nicks) {
                    ret.put(nick, getUser(nick, null, null, null, null).get().getUUID());
                }
                return ret;
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<Map<UUID, String>> getUsersNicks(List<UUID> uuids, ResponseCallback<Map<UUID, String>> callback,
                                                   ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                Map<UUID, String> ret = new HashMap<>();
                for (UUID uuid : uuids) {
                    UserInfo userInfo = uuidToUserInfo.get(uuid);
                    if (userInfo == null)
                        continue;
                    ret.put(uuid, userInfo.getCurrentNick());
                }
                return ret;
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> subscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                             ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                nickChangeListeners.add(listener);
            }
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeNickChanges(UserNickChangeListener listener, ResponseCallback<Void> callback,
                                               ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                nickChangeListeners.remove(listener);
            }
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> setUserNick(UUID user, String newNick, ResponseCallback<Void> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                UserInfo userInfo = uuidToUserInfo.get(user);
                String oldNick = userInfo.getCurrentNick();
                userInfo.setCurrentNick(newNick);
                if (nickToUserInfo.get(oldNick) == userInfo) {
                    nickToUserInfo.remove(oldNick);
                    nickToUserInfo.put(newNick, userInfo);
                }
                for (UserNickChangeListener listener : nickChangeListeners)
                    listener.onNickChanged(userInfo, oldNick, newNick);
            }
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> setUserChannelPresence(UUID user, String channel, boolean present,
                                               ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                UserInfo userInfo = uuidToUserInfo.get(user);
                userInfo.setChannelPresence(channel, present);
            }
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> clearAllUsersChannelPresences(ResponseCallback<Void> callback,
                                                      ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (SimpleUserInfoApi.this) {
                for (UserInfo userInfo : uuidToUserInfo.values())
                    userInfo.clearChannelPresences();
            }
            return null;
        }, callback, errorCallback);
    }

}
