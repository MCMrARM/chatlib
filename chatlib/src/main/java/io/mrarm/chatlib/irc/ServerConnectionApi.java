package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.*;
import io.mrarm.chatlib.dto.*;
import io.mrarm.chatlib.user.UserInfoApi;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public abstract class ServerConnectionApi implements ChatApi {

    private ServerConnectionData serverConnectionData;
    private final Object motdReceiveLock = new Object();
    private boolean motdReceived = false;
    private boolean motdReceiveFailed = false;

    public ServerConnectionApi(ServerConnectionData serverConnectionData) {
        this.serverConnectionData = serverConnectionData;
        serverConnectionData.setApi(this);
    }

    public ServerConnectionData getServerConnectionData() {
        return serverConnectionData;
    }

    @Override
    public UserInfoApi getUserInfoApi() {
        return serverConnectionData.getUserInfoApi();
    }

    protected void resetMotdStatus() {
        synchronized (motdReceiveLock) {
            motdReceived = false;
            motdReceiveFailed = false;
        }
    }

    public void notifyMotdReceived() {
        synchronized (motdReceiveLock) {
            motdReceived = true;
            motdReceiveLock.notifyAll();
        }
    }

    public void notifyMotdReceiveFailed() {
        synchronized (motdReceiveLock) {
            motdReceiveFailed = true;
            motdReceiveLock.notifyAll();
        }
    }

    protected boolean hasReceivedMotd() {
        synchronized (motdReceiveLock) {
            return motdReceived;
        }
    }

    protected boolean waitForMotd() {
        synchronized (motdReceiveLock) {
            while (!motdReceived && !motdReceiveFailed) {
                try {
                    motdReceiveLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
            return motdReceived && !motdReceiveFailed;
        }
    }

    public abstract void sendPong(String text);


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
            return new ChannelInfo(data.getName(), data.getTopic(), data.getMembersAsNickPrefixList());
        }, callback, errorCallback);
    }

    // TODO: This still isn't a deep clone of the message list, change it to one
    // TODO: 'after' parameter is currently disfunctional

    @Override
    public Future<MessageList> getMessages(String channelName, int count, MessageList after,
                                           ResponseCallback<MessageList> callback,
                                           ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            ChannelData data = getChannelData(channelName);
            List<MessageInfo> messages = data.getMessages();
            List<MessageInfo> ret = new ArrayList<>();
            synchronized (messages) {
                for (int i = Math.max(messages.size() - count, 0); i < messages.size(); i++)
                    ret.add(messages.get(i));
            }
            return new MessageList(ret);
        }, callback, errorCallback);
    }

    @Override
    public Future<StatusMessageList> getStatusMessages(int count, StatusMessageList after,
                                                       ResponseCallback<StatusMessageList> callback,
                                                       ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            List<StatusMessageInfo> messages = serverConnectionData.getServerStatusData().getMessages();
            List<StatusMessageInfo> ret = new ArrayList<>();
            synchronized (messages) {
                for (int i = Math.max(messages.size() - count, 0); i < messages.size(); i++)
                    ret.add(messages.get(i));
            }
            return new StatusMessageList(ret);
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
    public Future<Void> subscribeChannelInfo(String channelName, ChannelInfoListener listener,
                                             ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getChannelData(channelName).subscribeInfo(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeChannelInfo(String channelName, ChannelInfoListener listener,
                                               ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getChannelData(channelName).unsubscribeInfo(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> subscribeChannelMessages(String channelName, MessageListener listener,
                                                 ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            if (channelName == null)
                getServerConnectionData().subscribeMessages(listener);
            else
                getChannelData(channelName).subscribeMessages(listener);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeChannelMessages(String channelName, MessageListener listener,
                                                   ResponseCallback<Void> callback,
                                                   ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            if (channelName == null)
                getServerConnectionData().unsubscribeMessages(listener);
            else
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
