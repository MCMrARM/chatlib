package io.mrarm.chatlib.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import io.mrarm.chatlib.*;
import io.mrarm.chatlib.dto.ChannelInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.StatusMessageList;
import io.mrarm.chatlib.irc.ChannelData;
import io.mrarm.chatlib.irc.CommandHandlerList;
import io.mrarm.chatlib.irc.MessageHandler;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.user.SimpleUserInfoApi;
import io.mrarm.chatlib.user.UserInfoApi;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

public class TestApiImpl implements ChatApi {

    private ServerConnectionData serverConnectionData = new ServerConnectionData();

    public TestApiImpl(String nick) {
        serverConnectionData.setUserNick(nick);
        serverConnectionData.setUserInfoApi(new SimpleUserInfoApi());
    }

    public void readTestChatLog(BufferedReader reader) throws IOException {
        String line;
        CommandHandlerList commandHandlerList = new CommandHandlerList();
        commandHandlerList.addDefaultHandlers();
        MessageHandler handler = new MessageHandler(serverConnectionData, commandHandlerList);
        while ((line = reader.readLine()) != null) {
            try {
                handler.handleLine(line);
            } catch (Throwable t) {
                System.err.println("Failed to read test line: " + line);
                t.printStackTrace();
            }
        }
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
                                                   ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            getChannelData(channelName).unsubscribeMessages(listener);
            return null;
        }, callback, errorCallback);
    }

}
