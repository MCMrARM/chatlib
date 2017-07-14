package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PingCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[] { "PING" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        try {
            connection.getApi().sendCommand("PONG", true, params.get(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
