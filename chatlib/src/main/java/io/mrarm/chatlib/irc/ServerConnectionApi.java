package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.*;
import io.mrarm.chatlib.dto.ChannelInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.StatusMessageList;
import io.mrarm.chatlib.user.UserInfoApi;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.util.List;
import java.util.concurrent.Future;

public abstract class ServerConnectionApi implements ChatApi {

    private ServerConnectionData serverConnectionData;

    public ServerConnectionApi(ServerConnectionData serverConnectionData) {
        this.serverConnectionData = serverConnectionData;
    }

    public ServerConnectionData getServerConnectionData() {
        return serverConnectionData;
    }

    @Override
    public UserInfoApi getUserInfoApi() {
        return serverConnectionData.getUserInfoApi();
    }

    public ChannelData getChannelData(String channelName) throws NoSuchChannelException {
        return serverConnectionData.getJoinedChannelData(channelName);
    }

    @Override
    public Future<List<String>> getJoinedChannelList(ResponseCallback<List<String>> callback,
                                                     ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            return serverConnectionData.getJoinedChannelList();
        }, callback, errorCallback);
    }

    @Override
    public Future<ChannelInfo> getChannelInfo(String channelName, ResponseCallback<ChannelInfo> callback,
                                              ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            ChannelData data = getChannelData(channelName);
            return new ChannelInfo(data.getName(), data.getTitle(), data.getMembersAsNickPrefixList());
        }, callback, errorCallback);
    }

    // TODO: Return a copy of the MessageList instead of a reference

    @Override
    public Future<MessageList> getMessages(String channelName, int count, MessageList after,
                                           ResponseCallback<MessageList> callback,
                                           ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            ChannelData data = getChannelData(channelName);
            return new MessageList(data.getMessages());
        }, callback, errorCallback);
    }

    @Override
    public Future<StatusMessageList> getStatusMessages(StatusMessageList after,
                                                       ResponseCallback<StatusMessageList> callback,
                                                       ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            return new StatusMessageList(serverConnectionData.getServerStatusData().getMessages());
        }, callback, errorCallback);
    }

    public Future<Void> subscribeChannelList(ChannelListListener listener, ResponseCallback<Void> callback,
                                             ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getServerConnectionData().subscribeChannelList(listener);
            return null;
        }, callback, errorCallback);
    }

    public Future<Void> unsubscribeChannelList(ChannelListListener listener, ResponseCallback<Void> callback,
                                        ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getServerConnectionData().unsubscribeChannelList(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> subscribeChannelMessages(String channelName, MessageListener listener,
                                                 ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getChannelData(channelName).subscribeMessages(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeChannelMessages(String channelName, MessageListener listener,
                                                   ResponseCallback<Void> callback,
                                                   ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getChannelData(channelName).unsubscribeMessages(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> subscribeStatusMessages(StatusMessageListener listener, ResponseCallback<Void> callback,
                                                ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getServerConnectionData().getServerStatusData().subscribeMessages(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeStatusMessages(StatusMessageListener listener, ResponseCallback<Void> callback,
                                                  ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getServerConnectionData().getServerStatusData().unsubscribeMessages(listener);
            return null;
        }, callback, errorCallback);
    }

}
