package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.List;

public class PingCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[] { "PING" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params)
            throws InvalidMessageException {
        connection.getApi().sendPong(params.get(0), null, null);
    }

}
