package io.mrarm.chatlib.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.irc.CommandHandlerList;
import io.mrarm.chatlib.irc.MessageHandler;
import io.mrarm.chatlib.irc.ServerConnectionApi;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.user.SimpleUserInfoApi;

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
        throw new UnsupportedOperationException();
    }

    public void sendPong(String text) {
        throw new UnsupportedOperationException();
    }

}
