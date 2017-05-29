package io.mrarm.chatlib.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.SimpleUserInfoApi;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

public class TestApiImpl extends ServerConnectionApi {

    public TestApiImpl(String nick) {
        super(new ServerConnectionData());

        getServerConnectionData().setUserNick(nick);
        getServerConnectionData().setUserInfoApi(new SimpleUserInfoApi());
    }

    public void readTestChatLog(BufferedReader reader) throws IOException {
        String line;
        CommandHandlerList commandHandlerList = new CommandHandlerList();
        commandHandlerList.addDefaultHandlers();
        MessageHandler handler = new MessageHandler(getServerConnectionData(), commandHandlerList);
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
    public Future<Void> joinChannels(List<String> channels, ResponseCallback<Void> callback,
                                     ResponseErrorCallback errorCallback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> sendMessage(String channel, String message, ResponseCallback<Void> callback,
                                    ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            try {
                UUID userUUID = getUserInfoApi().resolveUser(getServerConnectionData().getUserNick(), null, null,
                        null, null).get();
                ChannelData channelData = getChannelData(channel);
                ChannelData.Member memberInfo = channelData.getMember(userUUID);
                MessageSenderInfo sender = new MessageSenderInfo(getServerConnectionData().getUserNick(), null, null,
                        memberInfo != null ? memberInfo.getNickPrefixes() : null, userUUID);
                channelData.addMessage(new MessageInfo(sender, new Date(), message, MessageInfo.MessageType.NORMAL));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, callback, errorCallback);
    }

    public void sendPong(String text) {
        throw new UnsupportedOperationException();
    }

}
