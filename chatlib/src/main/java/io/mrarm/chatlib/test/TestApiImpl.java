package io.mrarm.chatlib.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.SimpleAsyncChatApi;
import io.mrarm.chatlib.dto.ChannelInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.irc.ChannelData;
import io.mrarm.chatlib.irc.CommandHandlerList;
import io.mrarm.chatlib.irc.MessageHandler;
import io.mrarm.chatlib.irc.ServerConnectionData;

public class TestApiImpl extends SimpleAsyncChatApi {

    private ServerConnectionData serverConnectionData = new ServerConnectionData();

    public TestApiImpl(String nick) {
        serverConnectionData.setUserNick(nick);
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

    public ChannelData getChannelData(String channelName) throws NoSuchChannelException {
        return serverConnectionData.getJoinedChannelData(channelName);
    }

    @Override
    public void getJoinedChannelList(ResponseCallback<List<String>> callback,
                                     ResponseErrorCallback errorCallback) {
        queue(() -> {
            return serverConnectionData.getJoinedChannelList();
        }, callback, errorCallback);
    }

    @Override
    public void getChannelInfo(String channelName, ResponseCallback<ChannelInfo> callback,
                               ResponseErrorCallback errorCallback) {
        queue(() -> {
            ChannelData data = getChannelData(channelName);
            return new ChannelInfo(data.getName(), data.getTitle());
        }, callback, errorCallback);
    }

    @Override
    public void getMessages(String channelName, int count, MessageList after,
                            ResponseCallback<MessageList> callback,
                            ResponseErrorCallback errorCallback) {
        queue(() -> {
            ChannelData data = getChannelData(channelName);
            return new MessageList(data.getMessages());
        }, callback, errorCallback);
    }

}
