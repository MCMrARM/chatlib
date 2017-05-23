package io.mrarm.chatlib.irc;

import java.util.List;

public abstract class NumericCommandHandler implements CommandHandler {

    public abstract void handle(ServerConnectionData connection, MessagePrefix sender, int command,
                                List<String> params) throws InvalidMessageException;

    public void handle(ServerConnectionData connection, MessagePrefix sender, String command,
                       List<String> params)
            throws InvalidMessageException {
        int cmdId;
        try {
            cmdId = Integer.parseInt(command);
        } catch (NumberFormatException ex) {
            throw new InvalidMessageException("Invalid numerical command (" + command + ")");
        }
        handle(connection, sender, cmdId, params);
    }

}
