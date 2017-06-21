package io.mrarm.chatlib.irc;

import java.util.List;
import java.util.Map;

public interface CommandHandler {

    String[] getHandledCommands();

    void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                Map<String, String> tags)
            throws InvalidMessageException;

}
