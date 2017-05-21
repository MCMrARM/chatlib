package io.mrarm.chatlib.irc;

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
        String params = line.substring(commandEndI + 1);
        commandHandlerList.getHandlerFor(command).handle(connection, prefix, command, params);
    }

}
