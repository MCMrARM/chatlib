package io.mrarm.chatlib.irc;

import java.util.List;
import java.util.Map;

public interface CommandHandler {

    static int toNumeric(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    Object[] getHandledCommands();

    void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                Map<String, String> tags)
            throws InvalidMessageException;

}
