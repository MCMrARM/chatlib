package io.mrarm.chatlib.irc;

import java.util.List;
import java.util.Map;

public abstract class NumericCommandHandler implements CommandHandler {

    public abstract void handle(ServerConnectionData connection, MessagePrefix sender, int command,
                                List<String> params, Map<String, String> tags) throws InvalidMessageException;

    public abstract int[] getNumericHandledCommands();

    public String[] getHandledCommands() {
        int[] numeric = getNumericHandledCommands();
        String[] ret = new String[numeric.length];
        for (int i = 0; i < numeric.length; i++)
            ret[i] = String.format("%03d", numeric[i]);
        return ret;
    }

    public void handle(ServerConnectionData connection, MessagePrefix sender, String command,
                       List<String> params, Map<String, String> tags)
            throws InvalidMessageException {
        int cmdId;
        try {
            cmdId = Integer.parseInt(command);
        } catch (NumberFormatException ex) {
            throw new InvalidMessageException("Invalid numerical command (" + command + ")");
        }
        handle(connection, sender, cmdId, params, tags);
    }

}
