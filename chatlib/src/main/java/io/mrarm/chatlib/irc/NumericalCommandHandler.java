package io.mrarm.chatlib.irc;

public abstract class NumericalCommandHandler implements CommandHandler {

    public abstract void handle(ServerConnectionData connection, MessagePrefix sender, int command,
                                String params) throws InvalidMessageException;

    public void handle(ServerConnectionData connection, MessagePrefix sender, String command,
                       String params)
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
