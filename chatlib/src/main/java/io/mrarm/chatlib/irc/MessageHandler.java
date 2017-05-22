package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler {

    private ServerConnectionData connection;
    private CommandHandlerList commandHandlerList;

    public MessageHandler(ServerConnectionData connection, CommandHandlerList commandHandlerList) {
        this.connection = connection;
        this.commandHandlerList = commandHandlerList;
    }

    public CommandHandlerList getCommandHandlerList() {
        return commandHandlerList;
    }

    public void handleLine(String line) throws InvalidMessageException {
        if (line.length() == 0)
            return;
        MessagePrefix prefix = null;
        int prefixEndI = -1;
        if (line.startsWith(":")) {
            prefixEndI = line.indexOf(' ');
            if (prefixEndI == -1)
                throw new InvalidMessageException();
            prefix = new MessagePrefix(line.substring(1, prefixEndI));
        }
        int commandEndI = line.indexOf(' ', prefixEndI + 1);
        if (commandEndI == -1)
            throw new InvalidMessageException();
        String command = line.substring(prefixEndI + 1, commandEndI);
        String paramsRaw = line.substring(commandEndI + 1);
        List<String> params = parseParams(paramsRaw);
        commandHandlerList.getHandlerFor(command).handle(connection, prefix, command, params);
    }

    private List<String> parseParams(String paramsRaw) {
        List<String> params = new ArrayList<>();
        int i = 0;
        while (true) {
            if (paramsRaw.charAt(i) == ':') {
                params.add(paramsRaw.substring(i + 1));
                break;
            }
            int j = paramsRaw.indexOf(' ', i);
            if (j == -1) {
                params.add(paramsRaw.substring(i));
                break;
            } else {
                params.add(paramsRaw.substring(i, j));
            }
            i = j + 1;
        }
        return params;
    }

}
