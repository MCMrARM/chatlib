package io.mrarm.chatlib.irc;

import java.util.List;

public interface CommandHandler {

    String[] getHandledCommands();

    void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params)
            throws InvalidMessageException;

}
